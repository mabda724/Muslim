package com.Blackbox.muslim.data

object DailyDhikrData {

    data class DhikrItem(
        val arabic: String,
        val transliteration: String,
        val translation: String,
        val count: Int,
        val category: String,
        val reward: String
    )

    val salawatOnProphet = listOf(
        DhikrItem("اللهم صل وسلم على نبينا محمد", "Allahumma salli wa sallim 'ala nabiyyina Muhammad", "O Allah, send blessings and peace upon our Prophet Muhammad", 10, "صلاة", "wieght of 10 good deeds"),
        DhikrItem("اللهم صل على محمد وعلى آل محمد كما صليت على إبراهيم وعلى آل إبراهيم إنك حميد مجيد", "Salawat Ibrahimية", "Full salat Ibrahimية", 1, "صلاة", "10 blessings from Allah"),
        DhikrItem("اللهم صل وسلم وبارك على سيدنا محمد", "Allahumma salli wa sallim wa barik 'ala sayyidina Muhammad", "O Allah, send blessings, peace and blessings upon our Master Muhammad", 100, "صلاة", "all sins forgiven"),
        DhikrItem("صلوات الله عليه", "Salawatullah 'alayh", "The blessings of Allah be upon him", 10, "صلاة", "10 blessings returned"),
        DhikrItem("اللهم اجعل صلاتك وبركاتك ورحمةك على محمد وعلى آل محمد", "Allahumma ij'al salataka wa barataka wa rahmataka 'ala Muhammad wa ali Muhammad", "O Allah, make Your blessings, mercy and peace upon Muhammad and his family", 10, "صلاة", "peace in this world and hereafter")
    )

    val istighfar = listOf(
        DhikrItem("أستغفر الله", "Astaghfirullah", "I seek forgiveness from Allah", 100, "استغفار", "Allah will forgive sins"),
        DhikrItem("أستغفر الله العظيم", "Astaghfirullah al-'Adheem", "I seek forgiveness from Allah the Almighty", 100, "استغفار", "sins forgiven even if they reach the sky"),
        DhikrItem("اللهم إني ظلمت نفسي ظلما كثيرا ولا يغفر الذنوب إلا أنت فاغفر لي مغفرة من عندك وارحمني إنك أنت الغفور الرحيم", "Allahumma inni dhalamtu nafsi dhulman kathiran", "O Allah, I have wronged myself greatly, and none forgives sins except You, so grant me forgiveness from Yourself and have mercy upon me", 1, "استغفار", "Allah will forgive his sins"),
        DhikrItem("أستغفر الله وأتوب إليه", "Astaghfirullah wa atubu ilayh", "I seek forgiveness from Allah and repent to Him", 100, "استغفار", "sins forgiven and provision expanded"),
        DhikrItem("سبحان الله وبحمده سبحان الله العظيم", "SubhanAllahi wa bihamdihi SubhanAllahil 'Adheem", "Glory be to Allah and praise Him, Glory be to Allah the Almighty", 100, "استغفار", "beloved to the Most Merciful")
    )

    fun getDailyDhikr(): DhikrItem {
  return dailyAthkar.random()
}

val dailyAthkar = listOf(
        DhikrItem("سبحان الله", "SubhanAllah", "Glory be to Allah", 33, "ذكر", "100 hasanat"),
        DhikrItem("الحمد لله", "Alhamdulillah", "Praise be to Allah", 33, "ذكر", "100 hasanat"),
        DhikrItem("الله أكبر", "Allahu Akbar", "Allah is the Greatest", 34, "ذكر", "100 hasanat"),
        DhikrItem("لا إله إلا الله وحده لا شريك له", "La ilaha illAllahu wahdahu la sharika lah", "None has the right to be worshipped but Allah alone without partner", 100, "ذكر", "1000 hasanat or 1000 sins removed"),
        DhikrItem("حسبي الله لا إله إلا هو عليه توكلت", "HasbiyAllahu la ilaha illa Huwa, 'alayhi tawakkalt", "Allah is sufficient for me, there is no god but He, in Him I put my trust", 7, "ذكر", "Allah will suffice him"),
        DhikrItem("سبحان الله وبحمده سبحان الله العظيم", "SubhanAllahi wa bihamdihi SubhanAllahil 'Adheem", "Glory be to Allah and praise Him, Glory be to Allah the Almighty", 10, "ذكر", "beloved to Ar-Rahman"),
        DhikrItem("اللهم صل وسلم على نبينا محمد", "Allahumma salli wa sallim 'ala nabiyyina Muhammad", "O Allah, send blessings upon our Prophet", 10, "ذكر", "Allah will send 10 blessings"),
        DhikrItem("ما شاء الله لا قوة إلا بالله", "MashaAllahu la quwwata illa billah", "What Allah wills, there is no power except with Allah", 10, "ذكر", "protection from envy"),
        DhikrItem("بسم الله الذي لا يضر مع اسمه شيء", "Bismillahil-ladhi la yadurru ma'asmihi shai'un", "In the Name of Allah with Whose Name nothing can cause harm", 3, "ذكر", "nothing will harm him"),
        DhikrItem("أعوذ بكلمات الله التامات من شر ما خلق", "A'udhu bikalimatillahit-tammati min sharri ma khalaq", "I seek refuge in the perfect words of Allah from the evil of that which He has created", 3, "ذكر", "protection from evil")
    )

    val dailyTips = listOf(
        "لا تنسَ صلاة الفجر في وقتها فهي أثقل على الكافرين",
        "صلِّ على النبي ﷺ فإنها سبب لشفاعته لك يوم القيامة",
        "قل بسم الله عند أكلك وشربك فإن البركة تنزل",
        "لا تدخل الجنة حتى تؤمن ولا تؤمن حتى تحب",
        "أحب الأعمال إلى الله أدومها وإن قل",
        "من لا يشكر الناس لا يشكر الله",
        "الكلمة الطيبة صدقة",
        "إنما الأعمال بالنيات",
        "المؤمن الذي يخالط الناس ويصبر على أذاهم خير من الذي لا يخالط ولا يصبر",
        "تبسمك في وجه أخيك صدقة",
        "أقم الصلاة لذكرى",
        "إن الله لا يغير ما بقوم حتى يغيروا ما بأنفسهم",
        "ادعوني استجب لكم",
        "ورفعنا لك ذكرك",
        "ألا بذكر الله تطمئن القلوب",
        "إن مع العسر يسراً",
        "توكلت على الله ولا حول ولا قوة إلا بالله",
        "اللهم اجعلني من الصابرين",
        "اللهم اجعلني من الشاكرين",
        "اللهم اجعلني من عبادك الصالحين"
    )
}
