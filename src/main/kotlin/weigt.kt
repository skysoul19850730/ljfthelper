import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MCheckBox(text: String?, state: MutableState<Boolean>) {
    Row(Modifier.clickable {
        state.value = !state.value
    }) {
        Checkbox(state.value,null)
        Text(text ?: "", Modifier.align(Alignment.CenterVertically))
    }
}

@Composable
fun MRadioBUtton(text: String?, mId: Int, state: MutableState<Int>) {
    Row(Modifier.clickable {
        state.value = mId
    }) {
        RadioButton(mId == state.value,null)
        Text(text ?: "", Modifier.align(Alignment.CenterVertically))
    }
}