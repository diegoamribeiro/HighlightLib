package com.codenome.myhighlighttest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codenome.highlightview.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val highlightState = rememberHighlightState()

                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. Elemento de Cabeçalho Grande (Posição: Topo, Tamanho: Largo)
                            Card(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .highlightTarget("header", highlightState)
                                    .clickable {
                                        val tooltip = TooltipConfig("Cabeçalho principal. Útil para dar instruções globais da tela!")
                                        val config = HighlighterConfig(
                                            overlayColor = Color(0xB3000000),
                                            overlayRadius = 12.dp,
                                            overlayPadding = 12.dp,
                                            tooltipConfig = tooltip
                                        )
                                        highlightState.show("header", config)
                                    }
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Painel de Destaques", fontSize = 20.sp, style = MaterialTheme.typography.h6)
                                    Text("Clique nos elementos para testar o HighlightView em diferentes tamanhos e formatos.", fontSize = 14.sp, color = Color.Gray)
                                }
                            }

                            // 2. Elementos Intermediários de tamanhos variados lado a lado
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                Alignment.CenterVertically
                            ) {
                                // Ícone Pequeno (Tamanho: Pequeno, Formato: Círculo)
                                IconButton(
                                    onClick = {
                                        val tooltip = TooltipConfig(
                                            title = "Ícone de Informação!\nFormato circular ideal para botões de ajuda.",
                                            arrowDirection = ArrowDirection.TOP
                                        )
                                        val config = HighlighterConfig(
                                            overlayColor = Color(0xB31A0F2E), // Overlay roxo escuro
                                            overlayRadius = 30.dp,
                                            overlayPadding = 6.dp,
                                            tooltipConfig = tooltip
                                        )
                                        highlightState.show("small_icon", config)
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFFE3F2FD), CircleShape)
                                        .highlightTarget("small_icon", highlightState)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF1E88E5))
                                }

                                // Bloco Personalizado (Tamanho: Médio Retangular)
                                Box(
                                    modifier = Modifier
                                        .size(width = 120.dp, height = 70.dp)
                                        .background(Color(0xFFFFF3E0), RoundedCornerShape(16.dp))
                                        .highlightTarget("medium_box", highlightState)
                                        .clickable {
                                            val tooltip = TooltipConfig(
                                                title = "Bloco retangular médio arredondado.",
                                                backgroundColor = Color(0xFFE65100),
                                                arrowDirection = ArrowDirection.BOTTOM
                                            )
                                            val config = HighlighterConfig(
                                                overlayColor = Color(0xCC000000),
                                                overlayRadius = 16.dp,
                                                overlayPadding = 8.dp,
                                                tooltipConfig = tooltip
                                            )
                                            highlightState.show("medium_box", config)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Retângulo", color = Color(0xFFE65100), fontSize = 16.sp)
                                }

                                // Perfil Circular (Tamanho: Médio Circular)
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9))
                                        .highlightTarget("circle_profile", highlightState)
                                        .clickable {
                                            val tooltip = TooltipConfig(
                                                title = "Foto de perfil circular.\nDestaque focado no avatar.",
                                                backgroundColor = Color(0xFF2E7D32)
                                            )
                                            val config = HighlighterConfig(
                                                overlayColor = Color(0xCC1B5E20), // Overlay verde
                                                overlayRadius = 40.dp,
                                                overlayPadding = 4.dp,
                                                tooltipConfig = tooltip
                                            )
                                            highlightState.show("circle_profile", config)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = "Favorite", tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 3. Botão para Iniciar o Tour Sequencial Completo
                            Button(
                                onClick = {
                                    // Iniciando o tour passo a passo através do callback de dismiss
                                    val configHeader = HighlighterConfig(
                                        overlayRadius = 12.dp,
                                        tooltipConfig = TooltipConfig("Passo 1: Este é o cabeçalho.")
                                    )
                                    val configIcon = HighlighterConfig(
                                        overlayRadius = 30.dp,
                                        tooltipConfig = TooltipConfig("Passo 2: Aqui você acessa ajuda.")
                                    )
                                    val configBox = HighlighterConfig(
                                        overlayRadius = 16.dp,
                                        tooltipConfig = TooltipConfig("Passo 3: Bloco retangular.")
                                    )
                                    val configProfile = HighlighterConfig(
                                        overlayRadius = 40.dp,
                                        tooltipConfig = TooltipConfig("Passo 4: Atalho para o perfil.")
                                    )
                                    val configFab = HighlighterConfig(
                                        overlayRadius = 30.dp,
                                        tooltipConfig = TooltipConfig("Passo 5: Botão de ação rápida!")
                                    )

                                    // Executa em cadeia
                                    highlightState.show("header", configHeader) {
                                        highlightState.show("small_icon", configIcon) {
                                            highlightState.show("medium_box", configBox) {
                                                highlightState.show("circle_profile", configProfile) {
                                                    highlightState.show("fab", configFab) {
                                                        Toast.makeText(this@MainActivity, "Tour concluído!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Iniciar Tour Guiado")
                            }
                        }

                        // 4. Floating Action Button (Posição: Canto inferior direito, Tamanho: Redondo Pequeno/Médio)
                        FloatingActionButton(
                            onClick = {
                                val tooltip = TooltipConfig(
                                    title = "Botão de Compartilhar!\nPosicionado no canto inferior da tela.",
                                    backgroundColor = Color(0xFF6200EE)
                                )
                                val config = HighlighterConfig(
                                    overlayColor = Color(0xCC000000),
                                    overlayRadius = 35.dp,
                                    overlayPadding = 6.dp,
                                    tooltipConfig = tooltip
                                )
                                highlightState.show("fab", config)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .highlightTarget("fab", highlightState)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar")
                        } // Fim do Box interno com padding

                        // O Overlay que desenha o highlight cobrindo toda a tela de forma reativa
                        HighlightOverlay(
                            state = highlightState,
                            modifier = Modifier.fillMaxSize()
                        )
                    } // Fim do Box externo sem padding
                }
            }
        }
    }
}}