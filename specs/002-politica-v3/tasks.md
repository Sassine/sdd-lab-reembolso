# Tarefas 002 · Política de Despesas v3

> `[P]` = pode rodar em paralelo com outras `[P]` da mesma fase.
> Caminhos relativos a `src/main/java/com/orbe/reembolso/` e `src/test/java/com/orbe/reembolso/`.

## Fase 1 · Fundação

- [x] T001 [R2,R4,R8] Novo status, papel de aprovador e transições na entidade
      Arquivos: `dominio/StatusReembolso.java`, `dominio/PapelAprovador.java`, `dominio/Reembolso.java`, `dominio/ReembolsoTransicoesTest.java`
      Verificação: `./mvnw -q test -Dtest=ReembolsoTransicoesTest`
- [x] T002 [R7] Versão otimista na entidade e 409 para conflito de concorrência
      Arquivos: `dominio/Reembolso.java`, `api/TratadorDeErros.java`
      Verificação: `./mvnw -q test`

## Fase 2 · Políticas

- [x] T003 [P] [R1,R2,R3] PoliticaDeAlcada com limites de R$ 500,00 e R$ 5.000,00
      Arquivos: `servico/PoliticaDeAlcada.java`, `servico/PoliticaDeAlcadaTest.java`
      Verificação: `./mvnw -q test -Dtest=PoliticaDeAlcadaTest`
- [x] T004 [P] [R5] PoliticaDeSegregacao
      Arquivos: `servico/PoliticaDeSegregacao.java`, `servico/PoliticaDeSegregacaoTest.java`
      Verificação: `./mvnw -q test -Dtest=PoliticaDeSegregacaoTest`
- [x] T005 [P] [R6] PoliticaTetoRefeicao e soma diária no repositório
      Arquivos: `servico/PoliticaTetoRefeicao.java`, `servico/ReembolsoRepository.java`, `servico/PoliticaTetoRefeicaoTest.java`
      Verificação: `./mvnw -q test -Dtest=PoliticaTetoRefeicaoTest`

## Fase 3 · Integração

- [x] T006 [R1,R2,R3,R4,R5,R6,R8] Serviço e API usam as políticas; aprovação exige papel
      Arquivos: `servico/ReembolsoService.java`, `api/AprovacaoRequest.java`, `api/ReembolsoController.java`, `api/ReembolsoResponse.java`, `dominio/Reembolso.java`, `api/ReembolsoApiTest.java`, `api/AprovacaoPorAlcadaApiTest.java`
      Verificação: `./scripts/verify.sh`
- [x] T007 [R7] Teste de duas aprovações simultâneas
      Arquivos: `servico/ConcorrenciaAprovacaoTest.java`
      Verificação: `./mvnw -q test -Dtest=ConcorrenciaAprovacaoTest`

## Fase 4 · Acabamento

- [x] T008 [INFRA] Atualizar a spec 001: aprovação simples foi substituída pela 002
      Arquivos: `specs/001-reembolso-basico/spec.md`
      Verificação: `java scripts/SddCheck.java`
