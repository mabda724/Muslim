package com.Blackbox.muslim.data

object AthkarData {

    data class Thikr(val arabic: String, val translation: String, val count: Int)

    val morningAthkar = listOf(
        Thikr("أصبحنا وأصبح الملك لله", "We have reached the morning and at this very time all sovereignty belongs to Allah", 1),
        Thikr("اللهم بك أصبحنا وبك أمسينا وبك نحيا وبك نموت وإليك النشور", "O Allah, by You we have reached the morning, and by You we reach the evening, by You we live and die, and to You is the resurrection", 1),
        Thikr("سبحان الله وبحمده", "Glory be to Allah and praise Him", 100),
        Thikr("لا إله إلا الله وحده لا شريك له له الملك وله الحمد وهو على كل شيء قير", "None has the right to be worshipped but Allah, alone, without partner. To Him belongs sovereignty and praise and He is over all things omnipotent", 100),
        Thikr("أستغفر الله وأتوب إليه", "I seek forgiveness from Allah and repent to Him", 100),
        Thikr("بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم", "In the Name of Allah with Whose Name nothing on earth or heaven can cause harm, and He is the All-Hearing, the All-Knowing", 3),
        Thikr("اللهم إني أسألك العافية في الدنيا والآخرة", "O Allah, I ask You for well-being in this world and the Hereafter", 3),
        Thikr("حسبي الله لا إله إلا هو عليه توكلت وهو رب العرش العظيم", "Allah is sufficient for me. There is no god but He. In Him I put my trust, and He is the Lord of the Great Throne", 7),
        Thikr("سبحان الله وبحمده سبحان الله العظيم", "Glory be to Allah and praise Him. Glory be to Allah the Almighty", 100),
        Thikr("اللهم صل وسلم على نبينا محمد", "O Allah, send blessings and peace upon our Prophet Muhammad", 100)
    )

    val eveningAthkar = listOf(
        Thikr("أمسينا وأمسى الملك لله", "We have reached the evening and at this very time all sovereignty belongs to Allah", 1),
        Thikr("اللهم بك أمسينا وبك أصبحنا وبك نحيا وبك نموت وإليك المحشور", "O Allah, by You we have reached the evening and by You we reach the morning, by You we live and die, and to You is the gathering", 1),
        Thikr("أستغفر الله وأتوب إليه", "I seek forgiveness from Allah and repent to Him", 100),
        Thikr("سبحان الله وبحمده", "Glory be to Allah and praise Him", 100),
        Thikr("لا حول ولا قوة إلا بالله", "There is no power nor might except with Allah", 100),
        Thikr("بسم الله الذي لا يضر مع اسمه شيء في الأرض ولا في السماء وهو السميع العليم", "In the Name of Allah with Whose Name nothing on earth or heaven can cause harm, and He is the All-Hearing, the All-Knowing", 3),
        Thikr("اللهم إني أسألك العافية في الدنيا والآخرة", "O Allah, I ask You for well-being in this world and the Hereafter", 3),
        Thikr("أعوذ بكلمات الله التامات من شر ما خلق", "I seek refuge in the perfect words of Allah from the evil of that which He has created", 3),
        Thikr("اللهم صل وسلم على نبينا محمد", "O Allah, send blessings and peace upon our Prophet Muhammad", 100)
    )

    val sleepAthkar = listOf(
        Thikr("باسمك اللهم أموت وأحيا", "In Your name, O Allah, I die and I live", 1),
        Thikr("سبحان الله", "Glory be to Allah", 33),
        Thikr("الحمد لله", "Praise be to Allah", 33),
        Thikr("الله أكبر", "Allah is the Greatest", 34),
        Thikr("آية الكرسي", "Verse of the Throne - Allah! There is no god except Him, the Ever-Living, the Sustainer of existence...", 1)
    )

    val wakeupAthkar = listOf(
        Thikr("الحمد لله الذي أحيانا بعد ما أماتنا وإ إليه النشور", "Praise be to Allah who gave us life after causing us to die and to Him is the resurrection", 1),
        Thikr("لا إله إلا الله وحده لا شريك له له الملك وله الحمد وهو على كل شيء قدير", "None has the right to be worshipped but Allah, alone, without partner...", 10),
        Thikr("سبحان الله", "Glory be to Allah", 10),
        Thikr("الحمد لله", "Praise be to Allah", 10),
        Thikr("الله أكبر", "Allah is the Greatest", 10)
    )

    val quotes = listOf(
        "إن مع العسر يسراً - Indeed, with hardship comes ease",
        "ومن يتوكل على الله فهو حسبه - And whoever relies upon Allah, then He is sufficient for him",
        "لا يكلف الله نفساً إلا وسعها - Allah does not burden a soul beyond that it can bear",
        "إن الله مع الصابرين - Indeed, Allah is with the patient",
        "فإنه مع الذين اتقوا والذين هم محسنون - Indeed, He is with those who fear Him and those who do good",
        "وقل ربي زدني علماً - And say, My Lord, increase me in knowledge",
        "إن الله لا يغير ما بقوم حتى يغيروا ما بأنفسهم - Allah does not change the condition of a people until they change what is in themselves",
        "ادعوني استجب لكم - Call upon Me; I will respond to you",
        "وعلى الله فليتوكل المؤمنون - And upon Allah let those who trust rely",
        "ألا بذكر الله تطمئن القلوب - Verily, in the remembrance of Allah do hearts find rest",
        "إن الله يحب التوابين ويحب المتطهرين - Indeed, Allah loves those who are constantly repenting and loves those who purify themselves",
        "وهو معكم أين ما كنتم - And He is with you wherever you are",
        "ما شاء الله لا قوة إلا بالله - Masha Allah, there is no power except with Allah",
        "اللهم اجعلنا من الذين يستمعون القول فيتبعون أحسنه - O Allah, make us among those who listen to the word and follow the best of it",
        "رب اشرح لي صربي ويسّر لي أمري - My Lord, ease for me my task and untie the knot from my tongue"
    )
}
