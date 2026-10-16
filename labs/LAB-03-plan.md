# Lab 03 · Planejar

**Objetivo:** separar o como do quê, com cada decisão justificada.

## 1. Spec ou plano?

Classifique cada frase. Teste: *se trocarmos de tecnologia, continua verdade?*

1. "Ninguém aprova o próprio pedido."
2. "Adicionar coluna `aprovador_gestor` na tabela `reembolso`."
3. "Acima de R$ 5.000,00 o pedido é recusado com orientação de usar adiantamento."
4. "Usar `@Version` para controle otimista de concorrência."
5. "Quando dois responsáveis aprovarem ao mesmo tempo, só uma aprovação vale."
6. "Criar `PoliticaDeAlcada` em `servico/`."

<details><summary>Resposta</summary>Spec: 1, 3, 5. Plano: 2, 4, 6. Repare que 5 e 4 são a mesma
preocupação em níveis diferentes: a spec diz o que precisa ser verdade, o plano escolhe
como garantir.</details>

## 2. Planejar

```
/sdd-plan specs/002-<nome>
```

Observe o agente delegar a leitura do código ao `pesquisador`. Abra o `research.md`:
cada achado precisa ter `Classe#metodo`.

## 3. Revisar o plano (o ponto de maior alavancagem)

Uma linha errada de plano vira centenas de linhas erradas de código. Gaste aqui o
tempo que você economizaria na revisão do código.

- [ ] A checagem da constitution está preenchida artigo por artigo
- [ ] O diagrama mermaid mostra a forma que você esperava
- [ ] Cada decisão tem alternativa e porquê
- [ ] Cada critério de aceite da spec aponta para um teste
- [ ] Nenhuma dependência nova sem justificativa
- [ ] A concorrência (R de "duas aprovações ao mesmo tempo") tem um mecanismo concreto

**Dica de revisão visual:** numa sessão nova, peça só o diagrama mermaid do fluxo de
aprovação a partir do plano. Ver a forma antes de ler o detalhe pega erro de desenho cedo.
