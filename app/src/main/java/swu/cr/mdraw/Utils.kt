package swu.cr.mdraw

import android.content.Context

fun dp2px(context: Context,dp:Int):Float{
    return (context.resources.displayMetrics.density*dp)

}