package com.Blackbox.muslim.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class HadithData(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("hadith_data", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // الأحاديث المحلية (للعمل بدون إنترنت)
    companion object {
        val localHadiths = listOf(
            HadithItem(
                id = 1,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى\"",
                english = "The Messenger of Allah (peace be upon him) said: \"Actions are but by intentions, and every man shall have only that which he intended\"",
                source = "صحيح البخاري",
                grade = "صحيح",
                category = "النية"
            ),
            HadithItem(
                id = 2,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"الدين النصيحة\"",
                english = "The Messenger of Allah (peace be upon him) said: \"The religion is sincerity\"",
                source = "صحيح مسلم",
                grade = "صحيح",
                category = "النصيحة"
            ),
            HadithItem(
                id = 3,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه\"",
                english = "The Messenger of Allah (peace be upon him) said: \"None of you truly believes until he loves for his brother what he loves for himself\"",
                source = "صحيح البخاري",
                grade = "صحيح",
                category = "الأخلاق"
            ),
            HadithItem(
                id = 4,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"المسلم من سلم المسلمون من لسانه ويده\"",
                english = "The Messenger of Allah (peace be upon him) said: \"A Muslim is one from whose tongue and hand other Muslims are safe\"",
                source = "صحيح البخاري",
                grade = "صحيح",
                category = "الأخلاق"
            ),
            HadithItem(
                id = 5,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"من كان يؤمن بالله واليوم الآخر فليقل خيراً أو ليصمت\"",
                english = "The Messenger of Allah (peace be upon him) said: \"Whoever believes in Allah and the Last Day should speak good or remain silent\"",
                source = "صحيح البخاري",
                grade = "صحيح",
                category = "الكلام"
            ),
            HadithItem(
                id = 6,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"الطهور شطر الإيمان\"",
                english = "The Messenger of Allah (peace be upon him) said: \"Cleanliness is half of faith\"",
                source = "صحيح مسلم",
                grade = "صحيح",
                category = "الطهارة"
            ),
            HadithItem(
                id = 7,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"إن الله جميل يحب الجمال\"",
                english = "The Messenger of Allah (peace be upon him) said: \"Allah is beautiful and loves beauty\"",
                source = "صحيح مسلم",
                grade = "صحيح",
                category = "التوحيد"
            ),
            HadithItem(
                id = 8,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"خيركم من تعلم القرآن وعلمه\"",
                english = "The Messenger of Allah (peace be upon him) said: \"The best among you are those who learn the Qur'an and teach it\"",
                source = "صحيح البخاري",
                grade = "صحيح",
                category = "القرآن"
            ),
            HadithItem(
                id = 9,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"الدال على الخير كفاعله\"",
                english = "The Messenger of Allah (peace be upon him) said: \"The one who guides to something good is like the one who does it\"",
                source = "صحيح مسلم",
                grade = "صحيح",
                category = "الأخلاق"
            ),
            HadithItem(
                id = 10,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"الرجل على دين خليله، فلينظر أحدكم من يخالل\"",
                english = "The Messenger of Allah (peace be upon him) said: \"A person is upon the religion of his friend, so let one of you look at whom he befriends\"",
                source = "سنن الترمذي",
                grade = "حسن",
                category = "الصحبة"
            ),
            HadithItem(
                id = 11,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"من سلك طريقاً يلتمس فيه علماً سهل الله له به طريقاً إلى الجنة\"",
                english = "The Messenger of Allah (peace be upon him) said: \"Whoever follows a path in pursuit of knowledge, Allah will make easy for him a path to Paradise\"",
                source = "صحيح مسلم",
                grade = "صحيح",
                category = "العلم"
            ),
            HadithItem(
                id = 12,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"إن الله يحب إذا عمل أحدكم عملاً أن يتقنه\"",
                english = "The Messenger of Allah (peace be upon him) said: \"Allah loves that when one of you does something, he does it well\"",
                source = "سنن ابن ماجة",
                grade = "حسن",
                category = "العمل"
            ),
            HadithItem(
                id = 13,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"الجنة تحت أقدام الأمهات\"",
                english = "The Messenger of Allah (peace be upon him) said: \"Paradise is at the feet of mothers\"",
                source = "سنن النسائي",
                grade = "حسن",
                category = "البر"
            ),
            HadithItem(
                id = 14,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"رب كلمة قالها سائق لا يلوي بها شيئاً élevéَ له بها في الجنة سبعين خريفاً\"",
                english = "The Messenger of Allah (peace be upon him) said: \"A word spoken by a person without thinking about its consequences may cause him to fall seventy years into Hell\"",
                source = "سنن الترمذي",
                grade = "حسن",
                category = "الكلام"
            ),
            HadithItem(
                id = 15,
                arabic = "قال رسول الله صلى الله عليه وسلم: \"احفظ الله يحفظك\"",
                english = "The Messenger of Allah (peace be upon him) said: \"Remember Allah and He will remember you\"",
                source = "سنن الترمذي",
                grade = "حسن",
                category = "الذكر"
            )
        )
    }

    data class HadithItem(
        val id: Int,
        val arabic: String,
        val english: String,
        val source: String,
        val grade: String,
        val category: String
    )

    // الحصول على حديث اليوم
    fun getDailyHadith(): HadithItem {
        val today = dateFormat.format(Date())
        val savedIndex = prefs.getInt("daily_hadith_index_$today", -1)

        return if (savedIndex != -1) {
            localHadiths[savedIndex % localHadiths.size]
        } else {
            val newIndex = (Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) % localHadiths.size
            prefs.edit().putInt("daily_hadith_index_$today", newIndex).apply()
            localHadiths[newIndex]
        }
    }

    // الحصول على حديث عشوائي
    fun getRandomHadith(): HadithItem {
        return localHadiths.random()
    }

    // الحصول على الأحاديث حسب الفئة
    fun getHadithsByCategory(category: String): List<HadithItem> {
        return localHadiths.filter { it.category == category }
    }

    // الحصول على جميع الفئات
    fun getAllCategories(): List<String> {
        return localHadiths.map { it.category }.distinct()
    }

    // حفظ الحديث في المفضلة
    fun saveFavoriteHadith(hadith: HadithItem) {
        val favorites = getFavoriteHadiths().toMutableSet()
        favorites.add(hadith.id)
        saveFavorites(favorites)
    }

    // إزالة من المفضلة
    fun removeFavoriteHadith(hadith: HadithItem) {
        val favorites = getFavoriteHadiths().toMutableSet()
        favorites.remove(hadith.id)
        saveFavorites(favorites)
    }

    // الحصول على المفضلة
    fun getFavoriteHadiths(): Set<Int> {
        val json = prefs.getString("favorite_hadiths", "[]")
        return try {
            val jsonArray = JSONArray(json)
            val set = mutableSetOf<Int>()
            for (i in 0 until jsonArray.length()) {
                set.add(jsonArray.getInt(i))
            }
            set
        } catch (e: Exception) {
            emptySet()
        }
    }

    // حفظ المفضلة
    private fun saveFavorites(favorites: Set<Int>) {
        val jsonArray = JSONArray()
        favorites.forEach { jsonArray.put(it) }
        prefs.edit().putString("favorite_hadiths", jsonArray.toString()).apply()
    }

    // التحقق من أن الحديث في المفضلة
    fun isFavorite(hadithId: Int): Boolean {
        return getFavoriteHadiths().contains(hadithId)
    }

    // جلب الأحاديث من الإنترنت (اختياري)
    suspend fun fetchHadithsFromApi(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // يمكن إضافة هذا لاحقاً إذا أردت استخدام API
                // val api = HadithApi.getInstance()
                // val response = api.getHadiths(API_KEY, "ar", 100, 1)
                // حفظ البيانات محلياً
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
