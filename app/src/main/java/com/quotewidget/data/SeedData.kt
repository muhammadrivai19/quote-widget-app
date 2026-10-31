package com.quotewidget.data

val SeedQuotes = listOf(
    "Quotes don't work until you do!" to null,
    "Kerjakan hari ini, banggakan esok hari." to "Anonim",
    "Small steps every day." to "Anonim",
    "Fokus pada progres, bukan sempurna." to null,
    "Jangan menunda yang bisa dicicil." to null,
    "Disiplin mengalahkan motivasi." to null,
    "Belajar 1% lebih baik tiap hari." to null,
    "Mulai dulu, sempurnakan nanti." to null,
    "Gagal itu data, bukan vonis." to null,
    "Jaga fokus, jaga energi." to null,
    "Sedikit tapi konsisten." to null,
    "Baca, catat, ulangi." to null,
    "Jangan bandingkan bab 1-mu dengan bab 20 orang lain." to null,
    "Selesaikan sebelum sempurna." to null,
    "Capek boleh, berhenti jangan." to null,
    "Do what you can, with what you have." to "Theodore Roosevelt",
    "It always seems impossible until it's done." to "Nelson Mandela",
    "Action is the foundational key to all success." to "Pablo Picasso",
    "Jatuh 7 kali, bangkit 8 kali." to "Pepatah Jepang",
    "Konsisten mengalahkan intens sesaat." to null
)

fun seedQuoteList(now: Long = System.currentTimeMillis()): List<Quote> =
    SeedQuotes.mapIndexed { i, (text, author) ->
        Quote(text = text, author = author, createdAt = now - i * 1000L)
    }
