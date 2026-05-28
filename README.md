# HighlightView KMP

O **HighlightView** é uma biblioteca Kotlin Multiplatform (KMP) leve e moderna desenvolvida em **Compose Multiplatform** para criar overlays de destaque (tutorial/onboarding guides) interativos no Android e iOS com 100% de código compartilhado.

---

## ✨ Recursos
* **Multiplataforma**: Funciona de forma idêntica no Android e iOS usando a mesma base de código Compose.
* **Formatos Flexíveis**: Recortes em formatos customizados (retângulos, círculos, ovais e retângulos arredondados) com controle preciso de raio (`overlayRadius`) e margem de destaque (`overlayPadding`).
* **Tooltips Inteligentes**: Balões de dica autolayout que calculam o espaço disponível na tela e mudam a direção da seta (cima, baixo, esquerda ou direita) para evitar cortes nas bordas da tela.
* **Tours Sequenciais**: Encadeie múltiplos destaques de forma contínua usando o callback de fechamento (`onDismissListener`).
* **Responsivo e Reativo**: Ajusta os destaques em tempo real caso a tela rotacione ou haja rolagem da tela.

---

## 📦 Instalação

Adicione o módulo `:highlightview` nas dependências do bloco `commonMain` do seu projeto Kotlin Multiplatform:

```kotlin
// shared/build.gradle.kts ou composeApp/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":highlightview"))
        }
    }
}
```

---

## 🚀 Como Usar

### 1. Criar o Estado na Tela
No seu componente Composable de tela, crie e lembre o estado do highlight:

```kotlin
val highlightState = rememberHighlightState()
```

### 2. Mapear os Elementos Alvo
Adicione o modificador `Modifier.highlightTarget` nos componentes que você deseja destacar:

```kotlin
Button(
    onClick = { /* ação */ },
    modifier = Modifier.highlightTarget("meu_botao_id", highlightState)
) {
    Text("Botão Alvo")
}
```

### 3. Exibir o Destaque
Chame a função `show` do `HighlightState` passando uma chave e as configurações de visualização:

```kotlin
highlightState.show(
    key = "meu_botao_id",
    config = HighlighterConfig(
        overlayColor = Color(0x99000000),      // Cor do fundo translúcido
        overlayRadius = 16.dp,                  // Raio de arredondamento do recorte
        overlayPadding = 8.dp,                  // Espaçamento do recorte ao redor do elemento
        tooltipConfig = TooltipConfig(
            title = "Este é o seu botão de ação principal!",
            backgroundColor = Color.Black,
            textColor = Color.White
        )
    ),
    onDismiss = {
        // Callback executado ao fechar (quando o usuário clica em qualquer parte da tela)
        println("Destaque fechado!")
    }
)
```

### 4. Adicionar o Overlay na Tela
Insira o `HighlightOverlay` no topo do seu layout raiz (usualmente dentro de um `Box`), garantindo que ele cubra toda a tela:

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // Sua UI normal
    MinhaInterfaceDaTela()

    // O Overlay do highlight
    HighlightOverlay(state = highlightState)
}
```

---

## 🧭 Criando um Tour Guiado (Passo a Passo)

Para guiar o usuário em uma sequência de explicações pela tela, você pode encadear as chamadas da função `show` aproveitando o callback de dismiss:

```kotlin
Button(
    onClick = {
        val config1 = HighlighterConfig(tooltipConfig = TooltipConfig("Passo 1: Este é o topo!"))
        val config2 = HighlighterConfig(tooltipConfig = TooltipConfig("Passo 2: Clique aqui para favoritar."))
        val config3 = HighlighterConfig(tooltipConfig = TooltipConfig("Passo 3: E aqui você compartilha."))

        highlightState.show("header", config1) {
            highlightState.show("favorite_btn", config2) {
                highlightState.show("share_btn", config3) {
                    // Tour finalizado
                }
            }
        }
    }
) {
    Text("Iniciar Tour")
}
```
