package dev.gmarques.controledenotificacoes.domain.usecase.managed_apps

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.gmarques.controledenotificacoes.di.entry_points.HiltEntryPoints
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import junit.framework.TestCase
import org.joda.time.LocalDateTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IsAppInBlockPeriodUseCaseTest {

    @Suppress("PrivatePropertyName")
    private var tuesday_12_20__20_05_25_day_3_ofWeek = LocalDateTime.now()
        .withYear(2025)
        .withMonthOfYear(5)
        .withDayOfMonth(20)
        .withHourOfDay(12)
        .withMinuteOfHour(20)
        .withSecondOfMinute(0)


    @Test
    fun ruleThatBlocksForever() {

        val rule = Rule(
            name = "",
            type = Rule.Type.RESTRICTIVE,
            condition = null,
            action = Rule.actionDefault,
            days = listOf(
                Rule.WeekDay.SUNDAY,
                Rule.WeekDay.MONDAY,
                Rule.WeekDay.TUESDAY,
                Rule.WeekDay.WEDNESDAY,
                Rule.WeekDay.THURSDAY,
                Rule.WeekDay.FRIDAY,
                Rule.WeekDay.SATURDAY,
            ),
            timeRanges = listOf(
                TimeRange(true)
            ),
        )

        val expectedResult = true // is app blocked right now?
        val result = HiltEntryPoints.isAppInBlockPeriodUseCase().invoke(rule, tuesday_12_20__20_05_25_day_3_ofWeek)


        TestCase.assertEquals(
            "\n   expect: $expectedResult, \nreceived:${result}\n",
            expectedResult,
            result
        )

    }

    @Test
    fun ruleThatAllowsForever() {

        val rule = Rule(
            name = "",
            type = Rule.Type.PERMISSIVE,

            days = listOf(
                Rule.WeekDay.SUNDAY,
                Rule.WeekDay.MONDAY,
                Rule.WeekDay.TUESDAY,
                Rule.WeekDay.WEDNESDAY,
                Rule.WeekDay.THURSDAY,
                Rule.WeekDay.FRIDAY,
                Rule.WeekDay.SATURDAY,
            ),
            condition = null,
            action = Rule.actionDefault,
            timeRanges = listOf(
                TimeRange(true)
            ),
        )

        val expectedResult = false // is app blocked right now?
        val result = HiltEntryPoints.isAppInBlockPeriodUseCase().invoke(rule, tuesday_12_20__20_05_25_day_3_ofWeek)


        TestCase.assertEquals(
            "\n   expect: $expectedResult, \nreceived:${result}\n",
            expectedResult,
            result
        )

    }

    @Test
    fun ruleThatBlocksRightNow() {

        val rule = Rule(
            name = "",
            type = Rule.Type.RESTRICTIVE,

            days = listOf(
                Rule.WeekDay.TUESDAY,
            ),
            condition = null,
            action = Rule.actionDefault,
            timeRanges = listOf(
                TimeRange(8, 0, 11, 0),
                TimeRange(12, 0, 18, 0),
                TimeRange(19, 0, 22, 0),
            ),
        )

        val expectedResult = true // is app blocked right now?
        val result = HiltEntryPoints.isAppInBlockPeriodUseCase().invoke(rule, tuesday_12_20__20_05_25_day_3_ofWeek)


        TestCase.assertEquals(
            "\n   expect: $expectedResult, \nreceived:${result}\n",
            expectedResult,
            result
        )

    }

    @Test
    fun ruleThatAllowsRightNow() {

        val rule = Rule(
            name = "",
            type = Rule.Type.PERMISSIVE,

            days = listOf(
                Rule.WeekDay.TUESDAY,
            ),
            condition = null,
            action = Rule.actionDefault,
            timeRanges = listOf(
                TimeRange(8, 0, 18, 0),
            ),
        )

        val expectedResult = false // is app blocked right now?
        val result = HiltEntryPoints.isAppInBlockPeriodUseCase().invoke(rule, tuesday_12_20__20_05_25_day_3_ofWeek)


        TestCase.assertEquals(
            "\n   expect: $expectedResult, \nreceived:${result}\n",
            expectedResult,
            result
        )

    }

}