package edu.ap.mobiledevrentingapp.deviceDetails

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit,
    disabledDates: List<Date>
) {
    val today = Calendar.getInstance()
    val context = LocalContext.current
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val allDisabledDates = mutableSetOf<Triple<Int, Int, Int>>()
    disabledDates.forEach { date ->
        val cal = Calendar.getInstance()
        cal.time = date
        allDisabledDates.add(
            Triple(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
        )
    }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = null,
        initialSelectedEndDateMillis = null,
        yearRange = IntRange(
            today.get(Calendar.YEAR),
            today.get(Calendar.YEAR) + 1
        ),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = utcTimeMillis
                
                val isNotPast = calendar.timeInMillis >= today.timeInMillis
                
                val dateTriple = Triple(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                val isNotDisabled = !allDisabledDates.contains(dateTriple)
                
                return isNotPast && isNotDisabled
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null && 
                         dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text(context.getString(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = { 
                Text(
                    context.getString(R.string.device_details_select_dates),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)
                )
            },
            headline = { 
                Text(
                    context.getString(R.string.device_details_select_dates_headline),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary,
                headlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContainerColor = Yellow40,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                todayContentColor = Yellow40,
                todayDateBorderColor = Yellow40
            )
        )
    }
}