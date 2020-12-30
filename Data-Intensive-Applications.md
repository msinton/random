# Designing Data-Intensive Applications (Book notes)

## Partitioning 

aka sharding

main reason: scalability

also and related - for performance - geared towards transactions or analytics

### Approaches
1) partition by key range (good for ranged queries, harder to avoid skew)
2) by hash of key (good for skew, bad for range queries)

Cassandra has a compromise - compound primary key, hashing first part, but indexing subsequent parts for sorting. Good for 1-many relationships. e.g. user posts in a timeframe

### Secondary indexes
1) partitioning secondary indexes by Document 
aka local index
each partition maintians own indexes (index updates simple)
bad = query all partitions? scatter/gather
 - tail latency amplification
 - used in Mongo, Riak, Cass, Elas, SolrCloud, VoltDB

2) partitioning secondary indexes by Term
aka global index
this can involve partitioning the indexes, but the row/document can live on another partition
good = reads can involve just the partitions it needs to (so faster) (e.g. query for colour:red then red is)
Q - but since the row/doc lives on aonther partition could still involve read from all partitions?
bad = writes slower and more complicated
 - data can be out of date

### Rebalancing
Things change: more queries, more data, machine failures

require:
- even load as result
- no disruption
- no more data moved than necessary

#### strategy: 
1) fixed number of partitions: (start with more than you need) then redistribution effort can be shared (take x from each node when add a node) 
- Riak, Elas, Couchbase, Vold
Correct number of starting positions is hard and could mean all data on one node initially.

2) dynamic partitioning: key-ranged partitions are a problem to configure manually, for that reason it is done dynamically
- HBase, RethinkDB, MongoDB
- split when grow, merged when shrink
- initial set of partitions can be configured (pre-splitting) so that load is shared
- can be used with hash-partitioned 

3) proportional to nodes: adding or removing nodes increases or decreases partitions
- random splitting of nodes (can produce unfair splits)
- Cassandra, Ketama
- Cassandra has an alternative splitting algorithm that avoids unfair splits
- requires hash-based partitioning

Auromatic or Manual Rebalancing?
- danger of cascading failure, since (during) an overloaded node may be concluded as dead and then cluster rebalances (again) which adds pressure.
- Couchbase, Riak, Vold require an admin to commit the suggested repartition

### Request Routing
general problem is service discovery

#### approaches:
1) clients contact any node and it may forward the request to another
Cassandra, Riak - gossip protocol
2) all request via a routing tier 
(can use ZooKeeper as responsible for knowing the correct routing)
Couchbase, HBase, SolrCloud, Kafka
3) clients must be aware of the partitioning

## Transactions

ACID: Atomicity, Consistency, Isolation, Durability

Transation support in MySQL, PostgresSQL, Oracle, SQL Server probably based on System R.

NoSQL was thought to mean no transactions but this is not true. Though most did abandon them. Transactions are not the antithesis of scalability, neither are they required for "serious applications" with "valuable data".

### Atomicity
Different meanings in other contexts. Here, abortability is apt.

### Consistency
Doesn't really belong since depends the application developer + atomicity and isolation.

### Isolation
Sometimes this is the stronger guarantee of serializable isolation = each transaction pretends it is the only one being run on the database. The result is the same as if the operations ran serially, though in reality they may have been concurrently with other transactions.

To combat race conditions. 

### Durability
No data loss even if there is a hardware fault or DB (node) crash.

May mean written to disk to write-ahead log.

DB must wait till completed before reporting transaction as complete.

#### Replication
Durability and replication both help towards the goal of no data-loss and should be used together and with backups!

- node death: 
  - D = data unavailable till node alive/data-transfer
  - R = remain available
 
- correlated fault (crash every node for a particular input)
 R = in-memory data lost

- in async replica system, recent writes may be lost when leader unavailable
- power cut, SSDs can violate data loss guarantees
- files/data on disk can corrupt

### Isolation Levels:
### Read committed
To me, this means: writes respect transactions, and can't read what hasn't been committed

1. only see data that has been committed (No dirty reads)
2. only overwrite committed data (No dirty writes)

### Repeatable read and Snapshot Isolation
To me, this means: reads are effectively acquire a lock in the same way as a write (but without the perf problems)

Often needed in order to make a backup - otherwise state can be inconsistent

### Implementation
a) version objects by the transaction Id affecting them

b) B-tree append only, root node changes with each update (CouchDB, Datomic, LMDB), each root provides a consistent snapshot. Requires a background process for garbage collection and compaction.

#### Repeatable read
No-one really knows what it means. Defined before Snapshot Isolation existed. There is a formal definition but most vendors don't adhere to it. MySQL and PSQL term their Snapshot Isolation as RR.

### Lost Updates
One of the concurrent write problems (as is dirty writes)

Example, 2 users trying to increment a counter. Result is as if only one increment done, since both "reads" of current value take the original and inc concurrently.

#### Atomic write operations
Lock on the object being read. aka _cursor stability_

  Update counters set value = value + 1 where key = 'foo';
  
Not always possible, e.g. updating a Wiki doc involves arbitrary text editing.

#### Explicit locking
Example, games where 2 players can move same piece and require game logic validation to pass first. The lock prevents a concurrent move.

   Begin transaction; select ... for update;
   -- check valid in application code
   Update ...; Commit;
  
? What if not valid - need to release with the commit.

I'm not keen on this - there is complexity that I think could be handled better by instead just ensuring all users of the same game have their actions processed as a stream in a single thread in the application.

#### Auto detection of lost updates
We could allow them to happen and then detect and abort a trx in the event.

Benefits
- Snapshot Isolation can be used for this detection. MySQL does not do this, so some argue it fails to provide true snapshot isolation.
- do not require application code to use any special features - can forget to use atomic operations and ok?

**Question I have:**
So a trx is aborted - which means the application sees a failed trx. Which means it can retry it. Is it always this simple?

#### Compare-and-set
Can fail because the where clause could be reading from an old version! So the set succeeds but the compare was actually false!

  basically: If value = x, set value = y
  
