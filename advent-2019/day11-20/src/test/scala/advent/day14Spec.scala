package advent

class day14Spec extends BaseSpec {

  import day14._

  describe("day14") {
    it("star 1") {
      println(recipe(parse(input)))
    }

    it("star 2") {
      println()
    }
  }

  def example0 = """9 ORE => 2 A
  8 ORE => 3 B
  7 ORE => 5 C
  3 A, 4 B => 1 AB
  5 B, 7 C => 1 BC
  4 C, 1 A => 1 CA
  2 AB, 3 BC, 4 CA => 1 FUEL""".split("\n")

  /**
    *
    * Result(13491,HashMap(NZVS -> 7, GPVTF -> 1, QDVJ -> 8, FUEL -> 0, XJWVT -> 0,
    *   PSHF -> 10, HKGWZ -> 3, KHKGT -> 3, DCFZ -> 5))
    * Result(13155,HashMap(NZVS -> 2, GPVTF -> 1, QDVJ -> 8, FUEL -> 0, XJWVT -> 0,
    *   PSHF -> 3, HKGWZ -> 3, KHKGT -> 3, DCFZ -> 5))

    * 13491 - (157 + 179)
    *
    * 13312
    *
    *
    *
    * 22 * (7 , 7 PSHF)
    *    10 PSHF
    *   8 PSHF
    * 8* (157 ORE) // 4 NZVS
    * 5* (165 ORE)
    * 48 + 5 + 12 => 11 * (177 ORE)
    * (157 DCFZ) => 27 * (165 ORE)
    * (172 PSHF) => 25 * (179 ORE)
    *
    *
    * I calc how many times I need to perform the reaction to get each input. This creates a new set of requirements
    * For each of these, if they are not from ORE then calculate the requirements again
    * The trick is to accumulate reaction requirements for all paths that require that chemical
    * when the quantity is known proceed to the next
    * but how can I know a chemicals requirements have all been accumulated?
    */
  def example1 = """157 ORE => 5 NZVS
  165 ORE => 6 DCFZ
  44 XJWVT, 5 KHKGT, 1 QDVJ, 29 NZVS, 9 GPVTF, 48 HKGWZ => 1 FUEL
  12 HKGWZ, 1 GPVTF, 8 PSHF => 9 QDVJ
  179 ORE => 7 PSHF
  177 ORE => 5 HKGWZ
  7 DCFZ, 7 PSHF => 2 XJWVT
  165 ORE => 2 GPVTF
  3 DCFZ, 7 NZVS, 5 HKGWZ, 10 PSHF => 8 KHKGT""".split("\n")

  /**
    * require 1 Fuel
    * therefore require reaction a
    * each input is required x y z
    * for x, require reaction p to produce n of x
    * and y may require reaction p to produce m of x
    *
    * until we know n and m we do not know how many of p0 or p1 is required. But we still know they are required.
    * what if a reaction from p0 requires more x?
    *
    * Is this a tree where Fuel is the root? Or where Ore is the root?
    * is a tree that expands then collapses? a graph really.
    * a node can be required by many and is produced by many
    */
  def input =
    """5 LKQCJ, 1 GDSDP, 2 HPXCL => 9 LVRSZ
  5 HPXCL, 5 PVJGF => 3 KZRTJ
  7 LVRSZ, 2 GFSZ => 5 FRWGJ
  9 ZPTXL, 5 HGXJH, 9 LQMT => 7 LVCXN
  2 LQMT, 2 PVJGF, 10 CKRVN => 9 VWJS
  2 VRMXL, 12 NBRCS, 2 WSXN => 7 GDSDP
  1 CKRP => 8 TBHVH
  1 SVMNB, 2 KZRTJ => 8 WKGQS
  6 LKQCJ, 8 HPXCL, 7 MPZH => 1 BQPG
  1 RCWL => 7 MPZH
  4 FGCMS, 2 LQMT, 1 LKQCJ => 1 KTBRM
  1 ZTCSK, 6 CXQB, 2 ZBZRT => 3 PVJGF
  7 DBNLM => 9 ZBZRT
  5 BGNQ, 2 WBPD, 5 KTBRM => 9 GFSZ
  6 XQBHG, 1 GPWVC => 8 CKFTS
  1 XWLQM, 29 XQBHG, 7 KPNWG => 5 BXVL
  6 TBHVH, 1 KTBRM => 7 HJGR
  1 LQMT, 14 KPNWG => 7 GPWVC
  18 LVCXN, 8 XVLT, 4 KPNWG, 13 LKQCJ, 12 MFJFW, 5 GZNJZ, 1 FLFT, 7 WBPD => 8 KZGD
  1 TBHVH => 1 VWKJ
  118 ORE => 2 CKRP
  2 LTCQX => 3 XQBHG
  1 GPWVC => 4 SMFQ
  6 CKRP => 4 RCWL
  39 LHZMD, 15 CKFTS, 26 HVBW, 57 KTBRM, 13 DFCM, 30 KZGD, 35 FPNB, 1 LKQCJ, 45 HJGR, 22 RCZS, 34 VWKJ => 1 FUEL
  1 BQPG, 2 BGNQ, 12 WBPD => 8 LTCQX
  2 WSXN => 2 HPXCL
  3 GRFPX => 5 XVLT
  1 LVRSZ => 3 SVMNB
  6 HLMT => 9 ZPTXL
  20 GFSZ => 5 GZNJZ
  1 RCWL => 9 KPNWG
  24 BGNQ, 31 KTBRM => 8 FLFT
  14 VSVG => 9 DBNLM
  191 ORE => 8 CXQB
  115 ORE => 2 SWVLZ
  17 KZRTJ, 13 KPNWG => 7 CKRVN
  9 BQPG => 4 XWLQM
  4 SMFQ, 2 GRFPX => 1 MFJFW
  6 CXQB, 4 CKRP, 2 BXVL, 5 GZNJZ, 3 VWJS, 1 FLFT, 4 KPNWG => 7 DFCM
  1 TBHVH => 6 BGNQ
  3 LQMT => 7 HLMT
  11 GDSDP => 4 WBPD
  2 KPNWG, 5 VWJS, 33 NBRCS => 7 NVDW
  5 GDSDP => 6 FGCMS
  1 GPWVC, 7 BGNQ, 1 FRWGJ => 8 GRFPX
  23 KTBRM, 11 VRMXL, 6 GPWVC => 5 SRJHK
  2 XQBHG, 1 GZNJZ => 3 HVBW
  1 ZTCSK => 4 WSXN
  1 XVLT, 5 HLMT, 1 ZPTXL, 2 HVBW, 7 NVDW, 1 WKGQS, 1 LTCQX, 5 MPZH => 3 FPNB
  16 SRJHK => 6 DWBW
  1 SVMNB, 1 VRMXL => 3 HGXJH
  133 ORE => 6 VSVG
  3 NBRCS, 1 FGCMS => 4 LQMT
  1 CKRP => 4 ZTCSK
  5 CKRVN, 1 FLFT => 1 RCZS
  4 ZTCSK, 15 RCWL => 9 LKQCJ
  1 SWVLZ => 8 NBRCS
  5 CKRP, 14 CXQB => 5 VRMXL
  1 SMFQ, 1 DWBW => 2 LHZMD"""
      .split("\n")
}
