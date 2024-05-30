## SD2324 Project Assignment #2

### Reference Solution

### Log Dan - 30/05/2024 18:30

#### Todos os ficheiros relacionados com o Serviço Shorts foram duplicados e renamed para ShortsRep;

##### Os métodos foram todos alterados de modo a que aceitem o Long version - o contador para o client/servidor nunca aceder ao estado anterior àquele que acedeu na última comunicação;

##### Falta modificar o JavaShortsRep, de modo a o integrar com o Kafka.

##### Falta também modificar os métodos Post, de modo a poder guardar o contador "version".

##### Por último, falta modificar o ShortsRepResource, de modo a enviar as requests com o "version" no header
