# Tarefas 001 · Reembolso básico

## Fase 1 · Fundação

- [x] T001 [R1] Criar entidade Reembolso, enums e repositório
      Arquivos: `dominio/Reembolso.java`, `dominio/Categoria.java`, `dominio/StatusReembolso.java`, `servico/ReembolsoRepository.java`
      Verificação: `./mvnw -q test-compile`
- [x] T002 [INFRA] Configurar Clock injetável e relógio fixo de teste
      Arquivos: `config/ClockConfig.java`, `suporte/RelogioDeTeste.java`
      Verificação: `./mvnw -q test-compile`

## Fase 2 · Regras

- [x] T003 [P] [R2,R3] Criar PoliticaDePrazo com testes
      Arquivos: `servico/PoliticaDePrazo.java`, `servico/PoliticaDePrazoTest.java`
      Verificação: `./mvnw -q test -Dtest=PoliticaDePrazoTest`
- [x] T004 [P] [R5,R6] Transições de status na entidade
      Arquivos: `dominio/Reembolso.java`, `dominio/TransicaoDeStatusInvalidaException.java`
      Verificação: `./mvnw -q test-compile`

## Fase 3 · Integração

- [x] T005 [R1,R2,R3,R4,R5,R6,R7] Serviço, API, tratamento de erros e testes de API
      Arquivos: `servico/ReembolsoService.java`, `api/ReembolsoController.java`, `api/TratadorDeErros.java`, `api/ReembolsoApiTest.java`
      Verificação: `./scripts/verify.sh`
