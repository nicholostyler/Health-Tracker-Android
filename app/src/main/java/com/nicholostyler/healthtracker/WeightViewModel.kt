package com.nicholostyler.healthtracker

import WeightRecord
import android.content.Context

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WeightViewModel() : ViewModel() {
    var _weights: MutableLiveData<List<WeightRecord>> = MutableLiveData<List<WeightRecord>>()
    var weights: MutableList<WeightRecord> = mutableListOf()
    var dialogOpen = mutableStateOf(false)
    var largestWeight = mutableStateOf(1.0)
    var smallestWeight = mutableStateOf(1.0)
    var weightToGoal = mutableStateOf(1.0)
    var totalWeightLost = mutableStateOf(1.0)
    var weightLoss30Days = mutableStateOf(1.0)
    var weightLoss7days = mutableStateOf(1.0)
    var weightLossThisYear = mutableStateOf(1.0)
    var medianWeightLoss = mutableStateOf(1.0)
    var currentWeight = mutableStateOf(1.0)
    //var UserProfile = mutableStateOf(Profile("Nicholos Tyler", 180.0, LocalDate.of(2022,Month.DECEMBER,22)))
    var NameUser= mutableStateOf("Nicholos Tyler")
    var GoalWeight = mutableStateOf(180.0)
    var GoalDate = mutableStateOf(LocalDate.of(2023,Month.DECEMBER, 22))


    init {
        viewModelScope.launch {
            loadWeights()
        }
    }

    fun setUserName(newName: String)
    {
        NameUser.value = newName
    }

    fun setGoalWeight(newWeight: Double)
    {
        GoalWeight.value = newWeight
        weightToGoal.value = (currentWeight.value - GoalWeight.value)

    }

    fun setGoalDate(newDate: LocalDate)
    {
        GoalDate.value = newDate
    }

    fun GenerateWeightLoss()
    {
        //loadWeights()
        val minWeights = weights.minWithOrNull(Comparator.comparingDouble {it.weight})
        val maxWeights = weights.maxWithOrNull(Comparator.comparingDouble {it.weight })
        if (minWeights != null && maxWeights != null) {
            smallestWeight.value = minWeights.weight
            largestWeight.value = maxWeights.weight
        }
        println("Minimum: : $minWeights")
        weightToGoal.value = (currentWeight.value - GoalWeight.value)
    }

    fun GetWeightLossWeek()
    {
        var weightsOfTheWeek = mutableListOf<Double>()
        var weekAgoDate: LocalDate = LocalDate.now().minusWeeks(1)
        var todayDate: LocalDate = LocalDate.now()
        for (weight in weights) {
            if (weight.date > weekAgoDate) {
                weightsOfTheWeek.add(weight.weight)
            }
        }

        if (weightsOfTheWeek.count() != 0)
        {
            val maxWeights = weightsOfTheWeek.maxWithOrNull(Comparator.comparingDouble {it})
            if (maxWeights != null)
            {
                weightLoss7days.value = maxWeights - currentWeight.value
            }
        }
    }

    fun GetWeightLossMonth()
    {
        var weightsOfTheMonth = mutableListOf<Double>()
        var monthAgoDate: LocalDate = LocalDate.now().minusWeeks(4)
        var todayDate: LocalDate = LocalDate.now()
        for (weight in weights) {
            if (weight.date > monthAgoDate) {
                weightsOfTheMonth.add(weight.weight)
            }
        }

        if (weightsOfTheMonth.isNotEmpty())
        {
            val maxWeights = weightsOfTheMonth.maxWithOrNull(Comparator.comparingDouble {it})
            if (maxWeights != null)
            {
                weightLoss30Days.value = maxWeights - currentWeight.value
            }
        }
    }

    fun GetWeightLossYear()
    {
        var weightsOfTheYear = mutableListOf<Double>()
        var yearAgoDate: LocalDate = LocalDate.now().minusYears(1)
        var todayDate: LocalDate = LocalDate.now()
        for (weight in weights) {
            if (weight.date > yearAgoDate) {
                weightsOfTheYear.add(weight.weight)
            }
        }

        if (weightsOfTheYear.isNotEmpty())
        {
            val maxWeights = weightsOfTheYear.maxWithOrNull(Comparator.comparingDouble {it})
            if (maxWeights != null)
            {
                weightLossThisYear.value = maxWeights - currentWeight.value
            }
        }
    }

    suspend fun getWeightList() : LiveData<List<WeightRecord>> {
        loadWeights()

        if (_weights == null)
        {
            _weights = MutableLiveData<List<WeightRecord>>();
        }
        return _weights
    }

    private suspend fun loadWeights()
    {
            val weightList: MutableList<WeightRecord> = ArrayList()
            // add dates from past year
            val dateYearAgo = LocalDate.of(2021, Month.AUGUST, 20)
            val dateYearAgo2 = LocalDate.of(2021, Month.AUGUST, 22)
            // Past 30 days
            val dateMonthAgo = LocalDate.of(2022, Month.AUGUST, 2)
            val dateMonthAgo2 = LocalDate.of(2022, Month.AUGUST, 5)

            // Past 7 Days
            val datePast7 = LocalDate.of(2022, Month.AUGUST, 17)
            val datePast72 = LocalDate.of(2022, Month.AUGUST, 16)

            weightList.add(WeightRecord(weight = 288.0, dateYearAgo))
            weightList.add(WeightRecord(weight = 288.0, dateYearAgo2))
            weightList.add(WeightRecord(weight = 270.0, dateMonthAgo))
            weightList.add(WeightRecord(weight = 270.0, dateMonthAgo2))
            weightList.add(WeightRecord(weight = 265.0, datePast7))
            weightList.add(WeightRecord(weight = 264.0, datePast72))
            currentWeight.value = 264.0
            _weights.setValue(weightList)
            weights = weightList
            GenerateWeightLoss()
            GetWeightLossWeek()
            GetWeightLossMonth()
            GetWeightLossYear()
    }

    fun AddWeight()
    {
        weights.add(WeightRecord(weight = 235.0, LocalDate.now()))
        _weights.value = weights
        currentWeight.value = 235.0
    }

    fun ChangeDialogOpen(newValue: Boolean)
    {
        dialogOpen.value = newValue
    }
}