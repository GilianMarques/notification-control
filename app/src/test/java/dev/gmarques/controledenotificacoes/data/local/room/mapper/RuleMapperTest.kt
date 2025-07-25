/*
 * MIT License
 *
 * Copyright (c) 2025 Gilian Marques Fernandes - linkedin.com/in/gilianmarques
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */


import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.gmarques.controledenotificacoes.data.local.room.entities.RuleEntity
import dev.gmarques.controledenotificacoes.data.local.room.mapper.RuleMapper
import dev.gmarques.controledenotificacoes.domain.model.Rule
import dev.gmarques.controledenotificacoes.domain.model.Rule.Type
import dev.gmarques.controledenotificacoes.domain.model.TimeRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class RuleMapperTest {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val timeRangeType = Types.newParameterizedType(List::class.java, TimeRange::class.java)
    private val timeRangeAdapter = moshi.adapter<List<TimeRange>>(timeRangeType)

    private val weekDayType = Types.newParameterizedType(List::class.java, Rule.WeekDay::class.java)
    private val weekDayAdapter = moshi.adapter<List<Rule.WeekDay>>(weekDayType)


    @Test
    fun `ao passar uma rule valida para mapToEntity deve retornar uma RuleEntity com valores corretos`() {
        val rule = Rule(
            id = "1",
            name = "Regra Teste",
            type = Type.PERMISSIVE,
            days = listOf(Rule.WeekDay.MONDAY, Rule.WeekDay.FRIDAY),
            condition = null,
            keepFullHistory = Rule.keepFullHistoryDefault,
            action = Rule.actionDefault,
            timeRanges = listOf(TimeRange(8, 0, 12, 0))
        )

        val entity = RuleMapper.mapToEntity(rule)

        assertEquals(rule.id, entity.id)
        assertEquals(rule.name, entity.name)
        assertEquals(rule.type, entity.ruleType)
        assertEquals(weekDayAdapter.toJson(rule.days), entity.days)
        assertEquals(timeRangeAdapter.toJson(rule.timeRanges), entity.timeRanges)
    }

    @Test
    fun `ao passar uma RuleEntity valida para mapToModel deve retornar uma Rule com valores correspondentes`() {

        val range = TimeRange(14, 0, 16, 0)

        val entity = RuleEntity(
            id = "2",
            name = "Entidade Teste",
            ruleType = Type.RESTRICTIVE,
            days = weekDayAdapter.toJson(listOf(Rule.WeekDay.TUESDAY, Rule.WeekDay.THURSDAY)),
            condition = null,
            keepFullHistory = Rule.keepFullHistoryDefault,
            action = Rule.actionDefault,
            timeRanges = timeRangeAdapter.toJson(listOf(range))
        )

        val rule = RuleMapper.mapToModel(entity)

        assertEquals(entity.id, rule.id)
        assertEquals(entity.name, rule.name)
        assertEquals(entity.ruleType, rule.type)
        assertEquals(listOf(Rule.WeekDay.TUESDAY, Rule.WeekDay.THURSDAY), rule.days)
        assertEquals(listOf(range), rule.timeRanges)
    }


}
