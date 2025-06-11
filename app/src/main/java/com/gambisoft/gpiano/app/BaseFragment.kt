package com.gambisoft.gpiano.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.virgo_common.common_libs.baseApp.SimpleFragment

abstract class BaseFragment<VB : ViewBinding>(bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB) :
	SimpleFragment<VB>(bindingInflater) {
}