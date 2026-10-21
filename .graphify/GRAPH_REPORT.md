# Graph Report - .  (2026-10-21)

## Corpus Check
- Corpus is ~3.914 words - fits in a single context window. You may not need a graph.

## Summary
- 112 nodes · 128 edges · 13 communities detected
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output
- Edge kinds: method: 68 · calls: 30 · contains: 18 · inherits: 7 · imports: 5


## Input Scope
- Requested: all
- Resolved: all (source: configured-default)
- Included files: 24 · Candidates: recursive
- Excluded: 0 untracked · 0 ignored · 0 sensitive · 0 missing committed

## Graph Freshness
- Built from Git commit: `03a28cb`
- Compare this hash to `git rev-parse HEAD` before trusting freshness-sensitive graph output.
## God Nodes (most connected - your core abstractions)
1. `Reembolso` - 18 edges
2. `ReembolsoApiTest` - 14 edges
3. `ReembolsoService` - 8 edges
4. `ReembolsoController` - 7 edges
5. `TratadorDeErros` - 7 edges
6. `ReembolsoRepository` - 6 edges
7. `PoliticaDePrazoTest` - 5 edges
8. `SddCheck` - 4 edges
9. `RecursoNaoEncontradoException` - 3 edges
10. `ReembolsoDuplicadoException` - 3 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Communities

### Community 0 - "Community 0"
Cohesion: 0.13
Nodes (1): Reembolso

### Community 1 - "Community 1"
Cohesion: 0.26
Nodes (1): ReembolsoApiTest

### Community 2 - "Community 2"
Cohesion: 0.15
Nodes (5): RecursoNaoEncontradoException, ReembolsoDuplicadoException, RegraDeNegocioException, TransicaoDeStatusInvalidaException, RuntimeException

### Community 3 - "Community 3"
Cohesion: 0.22
Nodes (4): JpaRepository, Long, Reembolso, ReembolsoRepository

### Community 4 - "Community 4"
Cohesion: 0.43
Nodes (1): TratadorDeErros

### Community 5 - "Community 5"
Cohesion: 0.43
Nodes (1): ReembolsoService

### Community 6 - "Community 6"
Cohesion: 0.29
Nodes (1): ReembolsoController

### Community 7 - "Community 7"
Cohesion: 0.33
Nodes (1): PoliticaDePrazoTest

### Community 8 - "Community 8"
Cohesion: 0.60
Nodes (1): SddCheck

### Community 9 - "Community 9"
Cohesion: 0.50
Nodes (1): PoliticaDePrazo

### Community 10 - "Community 10"
Cohesion: 0.67
Nodes (1): RelogioDeTeste

### Community 11 - "Community 11"
Cohesion: 0.67
Nodes (1): ClockConfig

### Community 12 - "Community 12"
Cohesion: 0.67
Nodes (1): ReembolsoApplication

## Knowledge Gaps
- **Thin community `Community 0`** (1 nodes): `Reembolso`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 1`** (1 nodes): `ReembolsoApiTest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 4`** (1 nodes): `TratadorDeErros`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 5`** (1 nodes): `ReembolsoService`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 6`** (1 nodes): `ReembolsoController`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 7`** (1 nodes): `PoliticaDePrazoTest`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 8`** (1 nodes): `SddCheck`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 9`** (1 nodes): `PoliticaDePrazo`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 10`** (1 nodes): `RelogioDeTeste`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 11`** (1 nodes): `ClockConfig`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Community 12`** (1 nodes): `ReembolsoApplication`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReembolsoService` connect `Community 5` to `Community 3`?**
  _High betweenness centrality (0.023) - this node is a cross-community bridge._
- **Why does `ReembolsoController` connect `Community 6` to `Community 3`?**
  _High betweenness centrality (0.021) - this node is a cross-community bridge._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.1286549707602339 - nodes in this community are weakly interconnected._