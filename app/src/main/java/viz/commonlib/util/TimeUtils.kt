package viz.commonlib.util

import androidx.fragment.app.FragmentManager
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter
import com.viz.tools.TimeFormat
import viz.commonlib.utils.R
import java.util.*

/**
 * @title: TimeUtils
 * @projectName InsuranceDoubleRecord
 * @description: Time Utils
 * @author wei
 * @date 2020-04-02 10:54
 */

object TimeUtils {
    private val dateFormat = "yyyy-MM-dd"
    fun pickDate(
        fragmentManager: FragmentManager,
        onResult: (year: Int, month: Int, day: Int, formatDate: String) -> Unit,
        format: String = dateFormat,
        selectYear: Int = -1,
        selectMonth: Int = -1,
        selectDay: Int = -1,
        minYear: Int = -1,
        minMonth: Int = -1,
        minDay: Int = -1,
        maxYear: Int = -1,
        maxMonth: Int = -1,
        maxDay: Int = -1
    ) {
        CalendarDatePickerDialogFragment().apply {
            setOnDateSetListener { dialog, year, monthOfYear, dayOfMonth ->
                var month = (monthOfYear + 1).toString()
                if (month.length == 1) {
                    month = "0$month"
                }
                var day = dayOfMonth.toString()
                if (day.length == 1) {
                    day = "0$day"
                }
                val formatDate = "$year-$month-$day"
                onResult.invoke(
                    year,
                    monthOfYear + 1,
                    dayOfMonth,
                    if (format == dateFormat) {
                        formatDate
                    } else {
                        TimeFormat.changeFormat(
                            formatDate,
                            dateFormat,
                            format
                        )
                    }
                )
            }
            firstDayOfWeek = Calendar.MONDAY
            if (selectYear != -1 && selectMonth != -1 && selectDay != -1) {
                setPreselectedDate(selectYear, selectMonth - 1, selectDay)
            } else {
                val c = Calendar.getInstance()
                var year = c.get(Calendar.YEAR)
                var month = c.get(Calendar.MONTH)
                var day = c.get(Calendar.DAY_OF_MONTH)
                setPreselectedDate(
                    year,
                    month,
                    day
                )
            }
            val startCD = if (minYear != -1 && minMonth != -1 && minDay != -1) {
                MonthAdapter.CalendarDay(minYear, minMonth - 1, minDay)
            } else {
                val c = Calendar.getInstance()
                MonthAdapter.CalendarDay(c)
            }
            val endCD = if (maxYear != -1 && maxMonth != -1 && maxDay != -1) {
                MonthAdapter.CalendarDay(maxYear, maxMonth - 1, maxDay)
            } else {
                null
            }
            setDateRange(startCD, endCD)
            setDoneText("确定")
            setCancelText("取消")
            setThemeCustom(R.style.MyCustomBetterPickersDialogs)
            show(fragmentManager, "date")
        }
    }
}