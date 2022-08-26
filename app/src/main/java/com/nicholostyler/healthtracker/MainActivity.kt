@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.nicholostyler.healthtracker

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nicholostyler.healthtracker.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val dataStore = PreferenceDataStore(LocalContext.current)
                // get username and set it to viewmodel

                val model: WeightViewModel = viewModel()

                val scope = rememberCoroutineScope()
                val windowSize = rememberWindowSizeClass()
                //model.GenerateViewModel()
                Modifier.background(MaterialTheme.colorScheme.background)
                WeightMainPage(windowSize, model, dataStore)
            }
        }
    }
}

//@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WeightMainPage(windowSize: WindowSize, viewModel: WeightViewModel, dataStore: PreferenceDataStore) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
    )
    systemUiController.setStatusBarColor(color = MaterialTheme.colorScheme.surface)
    var selectedItem by remember { mutableStateOf(0)}
    val items = listOf("Home", "Logbook", "Profile")
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarState()
    )
    if (windowSize == WindowSize.Compact)
    {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

            bottomBar = {
                NavigationBar() {
                    items.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                when (item) {
                                    "Logbook" -> Icon(Icons.Filled.Book, contentDescription = null)
                                    "Home" -> Icon(Icons.Filled.Home, contentDescription = null)
                                    "Profile" -> Icon(Icons.Filled.Face, contentDescription = null)
                                }
                            },
                            label = { Text(item) },
                            selected = selectedItem == index,
                            onClick = { selectedItem = index }
                        )
                    }
                }
                },

            topBar = {
                SmallTopAppBar(
                    title = {Text("Health Tracker")},

                )
                     },
            floatingActionButton = {
                FloatingActionButton(onClick = { viewModel.ChangeDialogOpen(true) }) {
                    Icon(Icons.Filled.Add, "")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                when (selectedItem) {
                    0 -> DashboardPage(viewModel)
                    1 -> {
                        LogbookPage(viewModel)
                    }
                    2 -> {
                        ProfilePage(viewModel, dataStore)
                    }
                }

            }
        }
    }
}

@Composable
fun ProfilePage(viewModel: WeightViewModel, dataStore: PreferenceDataStore) {
    Column(){
        val scope = rememberCoroutineScope()
        val userName = dataStore.getUserName.collectAsState(initial = "")
        val goalWeight = dataStore.getGoalWeight.collectAsState(initial = "0.0")
        val goalWeightDate = dataStore.getGoalDate.collectAsState(initial = "Dec 10 Never")

        Text(text = "Name: ${userName.value}")
        Text(text = "Current Weight: ${viewModel.currentWeight.value}")
        Text(text = "Goal Weight: ${goalWeight.value}")
        Text(text = "Goal Weight By: ${goalWeightDate.value}")
        Button(onClick = {
            scope.launch {
                dataStore.saveUserToPreferencesStore("Poop Pants", 180.0, LocalDate.now())

            }

        }){

        }
    }
}

@Composable
fun WeightHeader(name: String, currentDate: String, currentWeight: String)
{
    Column(Modifier.padding(10.dp)) {
        Text(text = "Nicholos Tyler - August 10, 2022")
        Text(text = "CURRENT WEIGHT")
        Text(text = "$currentWeight lbs")
    }
}

@Composable
fun ProgressCard(currentWeight: String, leftWeight: String, targetWeight: String, progressLeft: Float)
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ){
        Column(Modifier.padding(16.dp)){
            Text(text = "Your Progress")
            Row(Modifier.padding(top = 12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(text = "Current")
                    Text(text = currentWeight)
                }
                Column(Modifier.weight(1f)) {
                    Text(text = "Left")
                    Text(text = leftWeight)
                }
                Column(Modifier.weight(1f)) {
                    Text(text = "Target")
                    Text(text = targetWeight)
                }
            }
            LinearProgressIndicator(progress = progressLeft,
                Modifier
                    .padding(top = 30.dp)
                    .fillMaxWidth())
        }
    }
}

@Composable
fun StatisticCardGrid(lowestWeightWeek: Double, lowestWeightMonth: Double, lowestWeightYear: Double, lowestWeightMedian: Double)
{
    Column {
        StatisticCardRow(lowestWeightWeek, lowestWeightMonth)
        StatisticCardRowYear(lowestWeightYear, lowestWeightMedian)

    }

}

@Composable
fun StatisticCardRowYear(lowestWeightYear: Double, lowestWeightMedian: Double)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .weight(1f)
            //elevation = CardDefaults.elevatedCardElevation()
        ){
            Column(Modifier.padding(16.dp),verticalArrangement = Arrangement.spacedBy(8.dp)){
                Text(text = "Past Year")
                Text(text = lowestWeightYear.toString())

            }
        }
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .weight(1f),
            //elevation = CardDefaults.elevatedCardElevation()
        ){
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Text(text = "Median Weight")
                Text(text = lowestWeightMedian.toString())

            }
        }
    }
}

@Composable
fun StatisticCardRow(lowestWeight7: Double, lowestWeightMonth: Double)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .weight(1f),
        ){
            Column(Modifier.padding(16.dp),verticalArrangement = Arrangement.spacedBy(8.dp)){
                Text(text = "Past 7 Days")
                Text(text = lowestWeight7.toString())

            }
        }
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .weight(1f),

        ){
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Text(text = "Past 30 Days")
                Text(text = lowestWeightMonth.toString())

            }
        }
    }
}

@Composable
fun PredictionsCard()
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(8.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Predictions", Modifier.padding(bottom = 8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 12.dp),
                horizontalArrangement = Arrangement.Center

            ) {
                OutlinedButton(onClick = {}, Modifier.padding(end = 8.dp)) {
                    Column() {
                        Text(text = "Goal Rate")
                        Text(text = "2 lb")
                    }
                }
                OutlinedButton(onClick = {}) {
                    Column() {
                        Text(text = "Goal Rate")
                        Text(text = "2 lb")
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)){
                Text(
                    text = "216 lbs",
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = "On September 10",
                    modifier = Modifier
                        .weight(1f)
                )

                Text(
                    text = "Next Month",
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.End
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)){
                Text(
                    text = "216 lbs",
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = "On September 10",
                    modifier = Modifier
                        .weight(1f)
                )

                Text(
                    text = "Next Month",
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.End
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)){
                Text(
                    text = "216 lbs",
                    modifier = Modifier
                        .weight(1f)
                )
                Text(
                    text = "On September 10",
                    modifier = Modifier
                        .weight(1f)
                )

                Text(
                    text = "Next Month",
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun LogListItem(weight: String, date: String)
{
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(64.dp)
    ){
        Column(
            Modifier.align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.Center
        ){
            Text(text = date)
            Text(text = ".5 lb")
        }
        Spacer(Modifier.weight(1f))
        Column(
            Modifier
                .align(Alignment.CenterVertically)
                .padding(bottom = 5.dp),
            verticalArrangement = Arrangement.Center
        )
        {
            Text(
                text = weight,
                fontSize = 22.sp,
            )
        }
    }
    Divider(startIndent = 4.dp)
}

@Composable
fun DashboardPage(viewModel: WeightViewModel)
{
    val scrollState = rememberScrollState()
    val progressLeft: Float = (viewModel.GoalWeight.value / viewModel.currentWeight.value).toFloat()
    var userName = viewModel.NameUser.value
    var goalDate = viewModel.GoalDate.value.toString()
    var weightToGoal = viewModel.weightToGoal.value.toString()
    var goalWeight = viewModel.GoalWeight.value.toString()
    var currentWeight = viewModel.currentWeight.value.toString()
    var weightLoss7Days = viewModel.weightLoss7days.value
    var weightLoss30Days = viewModel.weightLoss30Days.value
    var weightLossThisYear = viewModel.weightLossThisYear.value
    var weightLossMedian = viewModel.medianWeightLoss.value

    Column(modifier = Modifier
        .verticalScroll(rememberScrollState()))
    {
        WeightHeader(userName, goalDate, currentWeight)
        StatisticCardGrid(weightLoss7Days, weightLoss30Days, weightLossThisYear, weightLossMedian)
        ProgressCard(currentWeight, weightToGoal, goalWeight, progressLeft)
        PredictionsCard()
    }
}

@Composable
fun LogbookPage(viewModel: WeightViewModel)
{
    val dtf2 = DateTimeFormatter.ofPattern("MM d uuuu");

    if (viewModel.dialogOpen.value) {
        AddWeightDialog(viewModel)
    }

    LazyColumn() {
        items(items = viewModel.weights)
        { weight -> LogListItem(weight = weight.weight.toString(), date = dtf2.format(weight.date))}
    }

    //Text("Hello logbook")
}

@Composable
fun AddWeightDialog(viewModel: WeightViewModel)
{

    // Declaring integer values
    // for year, month and day
    val mYear: Int
    val mMonth: Int
    val mDay: Int

    val mContext = LocalContext.current
    // Initializing a Calendar
    val mCalendar = Calendar.getInstance()

    // Fetching current year, month and day
    mYear = mCalendar.get(Calendar.YEAR)
    mMonth = mCalendar.get(Calendar.MONTH)
    mDay = mCalendar.get(Calendar.DAY_OF_MONTH)

    mCalendar.time = Date()

    // Declaring a string value to
    // store date in string format
    val mDate = remember { mutableStateOf("") }
    // Declaring DatePickerDialog and setting
    // initial values as current values (present year, month and day)
    val mDatePickerDialog = DatePickerDialog(
        mContext,
        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
            mDate.value = "$mDayOfMonth/${mMonth+1}/$mYear"
        }, mYear, mMonth, mDay
    )
    AlertDialog(

        onDismissRequest = { },
        title = { Text(text = "Add Weight") },
        text = {
               Column() {
                   val textState = remember { mutableStateOf(TextFieldValue())}
                   OutlinedButton(onClick = { mDatePickerDialog.show() }){
                       if (mDate.value == "")
                       {
                           Text("Open Date Picker")
                       }
                       else
                       {
                           Text(mDate.value)
                       }
                   }
                   TextField(
                       value = textState.value,
                       onValueChange = { textState.value = it },
                       placeholder = {Text(text="100 lbs")}
                   )
               }
        },
        confirmButton = {
                        Button(onClick = {
                            viewModel.AddWeight()
                            viewModel.ChangeDialogOpen(false)
                        }
                        ){
                            Text("Save")
                        }
        },
        dismissButton = {
            TextButton(onClick = {}){
                Text("Cancel")
            }
        },
    )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme(

    ){
        var selectedItem by remember { mutableStateOf(0)}
        val items = listOf(
            "Home", "Logbook", "Profile"
        )
        val icons = listOf(Icon(Icons.Filled.Home,contentDescription = null), Icon(Icons.Filled.Book, contentDescription = null), Icon(Icons.Filled.Face, contentDescription = null))
        NavigationBar() {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = {
                        when (item) {
                            "Logbook" -> Icon(Icons.Filled.Book, contentDescription = null)
                            "Home" -> Icon(Icons.Filled.Home, contentDescription = null)
                            "Profile" -> Icon(Icons.Filled.Face, contentDescription = null)
                        }
                    },
                        label = { Text(item) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                        )
                    }
            }
        }
    }

