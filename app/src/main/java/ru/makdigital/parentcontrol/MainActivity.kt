package ru.makdigital.parentcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.makdigital.parentcontrol.ui.ParentControlRoot
import ru.makdigital.parentcontrol.ui.theme.ParentControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ParentControlTheme { ParentControlRoot(viewModel()) } }
    }
}
