# Changelog

## 2026-02-10 - Falha no `mvn clean install` por `MockMvc` ausente

### O que aconteceu
Ao executar:

```bash
mvn clean install
```

o build falhou na fase de testes com o erro:

```text
No qualifying bean of type 'org.springframework.test.web.servlet.MockMvc' available
```

Arquivo afetado:
- `src/test/java/com/example/educationalqualityproject/controller/HomeControllerTest.java`

### Causa raiz (explicacao para alunos)
No teste foi usada a anotacao `@AutoConfigureWebMvc`, mas ela **nao registra** o bean `MockMvc` para injecao no teste.
Como o campo abaixo depende desse bean, a injecao falha:

```java
@Autowired
private MockMvc mockMvc;
```

### Como corrigir (passo a passo)
1. Abra o arquivo `src/test/java/com/example/educationalqualityproject/controller/HomeControllerTest.java`.
2. Troque o import:

```java
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
```

por:

```java
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
```

3. Troque a anotacao da classe:

```java
@AutoConfigureWebMvc
```

por:

```java
@AutoConfigureMockMvc
```

4. Salve o arquivo.
5. Rode novamente:

```bash
mvn clean install
```

6. Verifique se os testes passam.

### Exemplo final esperado do teste
```java
@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }
}
```

### Nota didatica
Resumo para memorizar:
- `@AutoConfigureWebMvc` configura infraestrutura MVC.
- `@AutoConfigureMockMvc` disponibiliza `MockMvc` para testes web.
