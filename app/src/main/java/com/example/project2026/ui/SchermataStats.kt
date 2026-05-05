package com.example.project2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2026.viewmodel.SessioneViewModel
import com.example.project2026.viewmodel.VeicoloViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.VerticalPosition

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun SchermataStats(
    sessioneViewModel: SessioneViewModel,
    veicoloViewModel: VeicoloViewModel
) {
    val puntiHeatmap by sessioneViewModel.puntiHeatmap.collectAsState()
    val statisticheSpese by sessioneViewModel.statisticheSpese.collectAsState()

    // Calcolo della somma totale e nomi veicoli
    val spesaTotale = remember(statisticheSpese) { statisticheSpese.sumOf { it.totale } }
    val nomiVeicoli = remember(statisticheSpese) { statisticheSpese.map { it.nome } }

    // Produttore per il grafico Vico
    val modelProducer = remember { CartesianChartModelProducer() }

    // Formatter per mostrare i nomi dei veicoli sull'asse X
    val bottomAxisValueFormatter = CartesianValueFormatter { x, _, _ ->
        nomiVeicoli.getOrNull(x.toInt()) ?: ""
    }

    LaunchedEffect(statisticheSpese) {
        if (statisticheSpese.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    series(statisticheSpese.map { it.totale.toFloat() })
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // PARTE SUPERIORE: HEATMAP GEOGRAFICA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(LatLng(41.9028, 12.4964), 5f)
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                MapEffect(puntiHeatmap) { map ->
                    if (puntiHeatmap.isNotEmpty()) {
                        val latLngList = puntiHeatmap.map { LatLng(it.latitudine, it.longitudine) }
                        val provider = HeatmapTileProvider.Builder()
                            .data(latLngList)
                            .radius(30)
                            .build()
                        map.addTileOverlay(TileOverlayOptions().tileProvider(provider))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "DENSITÀ PARCHEGGI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // PARTE INFERIORE: GRAFICO COSTI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SPESE PER VEICOLO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "TOTALE: ${String.format("%.2f", spesaTotale)}€",
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (statisticheSpese.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nessun dato di spesa disponibile.", color = Color.Gray)
                }
            } else {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                                rememberLineComponent(
                                    color = Color(0xFF4087FA), // Colore blu richiesto
                                    thickness = 16.dp,
                                )
                            ),
                            dataLabel = rememberTextComponent(color = Color.White, textSize = 10.sp),
                            dataLabelVerticalPosition = VerticalPosition.Top
                        ),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter,
                            label = rememberTextComponent(color=Color.White)),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
