package com.nicholostyler.healthtracker

import WeightRecord

class WeightListState(val weights: List<WeightRecord> = emptyList(), val largestWeight: Double = 0.0, val lowestWeight: Double = 0.0) {
}