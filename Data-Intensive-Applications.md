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
 D = data unavailable till node alive/data-transfer
 R = remain available
 
- correlated fault (crash every node for a particular input)
 R = in-memory data lost

- in async replica system, recent writes may be lost when leader unavailable
- power cut, SSDs can violate data loss guarantees
- files/data on disk can corrupt
