# Constitution — serviço de reembolso

Regras permanentes do projeto. Valem para toda spec, todo plano e toda tarefa.
Mudar um artigo exige registro em `docs/decisoes/` com o porquê.

> Teste para saber se algo pertence aqui: **vale para qualquer feature deste projeto?**
> Se só vale para uma mudança, vai na spec ou no plano daquela mudança.

## Art. 1 · Camadas

`api` (HTTP, DTOs, validação de formato) → `servico` (regras de negócio, transação)
→ `dominio` (entidades, estados, exceções). A camada de baixo não conhece a de cima.
Regra de negócio nunca fica no controller.

## Art. 2 · Dinheiro e datas

- Dinheiro é `BigDecimal` com 2 casas. Nunca `double` ou `float`.
- "Hoje" vem do `Clock` injetado. Nunca `LocalDate.now()` sem relógio.
- Fuso padrão: America/Sao_Paulo.

## Art. 3 · Políticas como componentes

Cada seção da Política de Despesas vira um componente pequeno e testável em
`servico/` (ex.: `PoliticaDePrazo`). O serviço orquestra as políticas; não as reimplementa.

## Art. 4 · Erros

Toda resposta de erro é Problem Details (RFC 9457) via `TratadorDeErros`.
- 400: formato inválido · 404: não encontrado
- 409: conflito de estado (duplicidade, transição inválida, concorrência)
- 422: regra de negócio violada

## Art. 5 · Testes

- Todo critério de aceite da spec vira pelo menos um teste automatizado.
- Teste existente não se apaga nem se enfraquece para "passar". Se um teste parece
  errado, pare e registre a dúvida no progress.md.
- Status 200 não é critério de aceite. O teste verifica o corpo e o estado resultante.

## Art. 6 · Dependências

Nenhuma dependência nova no `pom.xml` sem estar justificada no plano da mudança.

## Art. 7 · Segurança

Nada de segredo, token ou dado pessoal real em código, teste, log ou artefato de spec.
