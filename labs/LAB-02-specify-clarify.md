# Lab 02 · Especificar e esclarecer

**Objetivo:** transformar o TICKET-002 numa spec aprovada, sem deixar o modelo decidir
o que é do negócio.

## 1. Antes de qualquer agente

Leia `tickets/TICKET-002-politica-v3.md` e a seção 4.1 e 5 de
`docs/politica-de-despesas.md`. Anote três coisas que o documento **não** responde.

## 2. Especificar

```
/sdd-specify tickets/TICKET-002-politica-v3.md
```

Abra `specs/002-*/spec.md` e confira com este checklist:

- [ ] Todo requisito tem ID `**R1**`, `**R2**`… e formato QUANDO / SE … O SISTEMA DEVE
- [ ] Nenhuma classe, tabela, endpoint ou framework no texto
- [ ] Cada requisito tem critério de aceite com valores concretos
- [ ] Os critérios dizem o que volta **e** o que muda no estado (não só "retorna 200")
- [ ] Existe a seção "Comportamento que NÃO pode mudar" preenchida
- [ ] As lacunas viraram `[PRECISA ESCLARECER: ...]` em vez de suposições

## 3. Esclarecer

```
/sdd-clarify specs/002-<nome>
```

Responda as perguntas como se você fosse a PO. Se o agente **não** perguntou algo
destes, pergunte você e registre a decisão:

| Lacuna | Por que importa |
|---|---|
| O teto de R$ 120 é por colaborador ou por pessoa na mesa? | muda a soma |
| Pedidos rejeitados entram na soma do dia? | sem regra, cada implementação decide uma coisa |
| Acima do teto: recusa tudo ou aprova até o teto? | é decisão de negócio, não técnica |
| R$ 500,00 exatos: só gestor ou gestor + Financeiro? | "até" e "acima de" no limite |
| Dois gestores aprovando ao mesmo tempo | o ticket conta que já aconteceu |
| Os clientes atuais da API não mandam o papel de quem aprova | compatibilidade |

## 4. Morte silenciosa

Para cada requisito, pergunte: e se der erro aqui? E no limite exato? E se vier vazio?
E se chegar duas vezes ao mesmo tempo? O que não estiver escrito não vai ser tratado:
o agente segue a direção que você deu e não inventa o cenário que ninguém pediu.

## 5. Comparar

```bash
git show gabarito:specs/002-politica-v3/spec.md
```

Sua spec não precisa ser igual. Ela precisa ter respondido às mesmas perguntas.
