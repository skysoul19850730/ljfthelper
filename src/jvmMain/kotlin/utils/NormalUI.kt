import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VSpace(dp:Int){
    Spacer(Modifier.height(dp.dp))
}
@Composable
fun HSpace(dp:Int){
    Spacer(modifier = Modifier.width(dp.dp))
}
@Composable
fun VSpace16(){
    VSpace(dp = 16)
}

@Composable
fun HSpace16(){
    HSpace(dp = 16)
}
@Composable
fun Int.vSpace(){
    VSpace(this)
}
@Composable
fun Int.hSpace(){
    HSpace(this)
}