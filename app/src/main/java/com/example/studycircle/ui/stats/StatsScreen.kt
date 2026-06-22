package com.example.studycircle.ui.stats

import android.graphics.Color as AndroidColor
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.studycircle.ui.theme.GradientEnd
import com.example.studycircle.ui.theme.GradientStart
import com.example.studycircle.ui.theme.Primary
import com.example.studycircle.ui.theme.TextSecondary
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit = {}
) {
    // Sample data — in production this comes from Firestore
    val weeklyData = listOf(2.5f, 4f, 1.5f, 3f, 5f, 2f, 3.5f)
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val subjectData = mapOf(
        "DSA" to 8,
        "OS" to 5,
        "DBMS" to 4,
        "Math" to 6,
        "CS" to 3
    )
    val monthlyData = listOf(
        1f, 2f, 1.5f, 3f, 2.5f, 4f,
        3f, 5f, 4f, 3.5f, 4.5f, 5f,
        4f, 6f, 5f, 4.5f, 5.5f, 6f,
        5f, 7f, 6f, 5.5f, 6.5f, 7f,
        6f, 8f, 7f, 6.5f, 7.5f, 8f
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Study Stats",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    )
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats overview cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.PostAdd,
                    value = "12",
                    label = "Total Posts",
                    color = Color(0xFF6C5CE7),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.Timer,
                    value = "48h",
                    label = "Time Studied",
                    color = Color(0xFF00CEC9),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.Whatshot,
                    value = "7🔥",
                    label = "Day Streak",
                    color = Color(0xFFE17055),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.MenuBook,
                    value = "5",
                    label = "Subjects",
                    color = Color(0xFF00B894),
                    modifier = Modifier.weight(1f)
                )
            }

            // Weekly Activity Bar Chart
            ChartCard(title = "📊 Weekly Study Activity") {
                WeeklyBarChart(
                    data = weeklyData,
                    labels = weekDays
                )
            }

            // Subject Distribution Pie Chart
            ChartCard(title = "🥧 Subject Distribution") {
                SubjectPieChart(data = subjectData)
            }

            // Monthly Activity Line Chart
            ChartCard(title = "📈 Monthly Progress") {
                MonthlyLineChart(data = monthlyData)
            }

            // Subject breakdown list
            ChartCard(title = "📚 Subject Breakdown") {
                SubjectBreakdownList(data = subjectData)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically()
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<Float>,
    labels: List<String>
) {
    val primaryColor = Primary.toArgb()
    val surfaceColor = AndroidColor.TRANSPARENT

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                val entries = data.mapIndexed { index, value ->
                    BarEntry(index.toFloat(), value)
                }
                val dataSet = BarDataSet(entries, "Hours").apply {
                    color = primaryColor
                    valueTextSize = 10f
                    valueTextColor = AndroidColor.GRAY
                    setDrawValues(true)
                }
                this.data = BarData(dataSet).apply {
                    barWidth = 0.6f
                }

                // Styling
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setBackgroundColor(surfaceColor)
                setTouchEnabled(false)
                animateY(800)

                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(labels)
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    textColor = AndroidColor.GRAY
                    textSize = 11f
                }
                axisLeft.apply {
                    textColor = AndroidColor.GRAY
                    setDrawGridLines(true)
                    gridColor = AndroidColor.LTGRAY
                    axisMinimum = 0f
                    textSize = 11f
                }
                axisRight.isEnabled = false
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun SubjectPieChart(data: Map<String, Int>) {
    val colors = listOf(
        AndroidColor.parseColor("#6C5CE7"),
        AndroidColor.parseColor("#00CEC9"),
        AndroidColor.parseColor("#E17055"),
        AndroidColor.parseColor("#00B894"),
        AndroidColor.parseColor("#FDCB6E")
    )

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                val entries = data.entries.mapIndexed { _, entry ->
                    PieEntry(entry.value.toFloat(), entry.key)
                }
                val dataSet = PieDataSet(entries, "").apply {
                    this.colors = colors
                    valueTextSize = 12f
                    valueTextColor = AndroidColor.WHITE
                    sliceSpace = 3f
                }
                this.data = PieData(dataSet)

                // Styling
                description.isEnabled = false
                isDrawHoleEnabled = true
                holeRadius = 45f
                transparentCircleRadius = 50f
                setHoleColor(AndroidColor.TRANSPARENT)
                setCenterText("Subjects")
                setCenterTextSize(14f)
                setCenterTextColor(AndroidColor.GRAY)
                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    textColor = AndroidColor.GRAY
                }
                animateY(1000)
                setTouchEnabled(false)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}

@Composable
private fun MonthlyLineChart(data: List<Float>) {
    val primaryColor = Primary.toArgb()
    val gradientColor = GradientEnd.copy(alpha = 0.3f).toArgb()

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                val entries = data.mapIndexed { index, value ->
                    Entry(index.toFloat(), value)
                }
                val dataSet = LineDataSet(entries, "Study Hours").apply {
                    color = primaryColor
                    lineWidth = 2.5f
                    setDrawCircles(false)
                    setDrawValues(false)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    fillColor = primaryColor
                    fillAlpha = 50
                }
                this.data = LineData(dataSet)

                // Styling
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setBackgroundColor(AndroidColor.TRANSPARENT)
                setTouchEnabled(false)
                animateX(1000)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = AndroidColor.GRAY
                    granularity = 5f
                    textSize = 11f
                }
                axisLeft.apply {
                    textColor = AndroidColor.GRAY
                    setDrawGridLines(true)
                    gridColor = AndroidColor.LTGRAY
                    axisMinimum = 0f
                    textSize = 11f
                }
                axisRight.isEnabled = false
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun SubjectBreakdownList(data: Map<String, Int>) {
    val total = data.values.sum().toFloat()
    val colors = listOf(
        Color(0xFF6C5CE7),
        Color(0xFF00CEC9),
        Color(0xFFE17055),
        Color(0xFF00B894),
        Color(0xFFFDCB6E)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        data.entries.forEachIndexed { index, entry ->
            val percentage = (entry.value / total * 100).toInt()
            val color = colors[index % colors.size]

            var animatedProgress by remember { mutableStateOf(0f) }
            val progress by animateFloatAsState(
                targetValue = entry.value / total,
                animationSpec = tween(800, delayMillis = index * 100),
                label = "progress"
            )

            LaunchedEffect(Unit) { animatedProgress = entry.value / total }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = entry.key,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$percentage%",
                        fontSize = 13.sp,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}