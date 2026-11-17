package ru.rfdocs.wallet.data.model

@kotlinx.serialization.Serializable
data class DocTemplate(
    val typeKey: String,
    val title: String,
    val category: DocCategory,
    val gradientKey: String,
    val iconKey: String,
    val fields: List<DocField>
)

object DocTemplates {

    private fun f(
        label: String,
        placeholder: String = "",
        masked: Boolean = false,
        mono: Boolean = false,
        inputMask: String = "",
        multiline: Boolean = false
    ): DocField = DocField(
        label = label,
        value = "",
        masked = masked,
        mono = mono,
        inputMask = inputMask,
        multiline = multiline,
        placeholder = placeholder
    )

    val passport: DocTemplate = DocTemplate(
        typeKey = "PASSPORT_RF",
        title = "Паспорт РФ",
        category = DocCategory.IDENTITY,
        gradientKey = GradientKeys.RUBY,
        iconKey = IconKeys.PASSPORT,
        fields = listOf(
            f("Фамилия и имя", "Иванов Иван Иванович"),
            f("Пол", "М / Ж"),
            f("Дата рождения", "01.01.1990"),
            f("Место рождения", "г. Москва"),
            f("Серия и номер", "0000 000000", masked = true, mono = true, inputMask = DocField.MASK_PASSPORT),
            f("Кем выдан", "ГУ МВД России по г. Москве"),
            f("Дата выдачи", "01.01.2020"),
            f("Код подразделения", "000-000", mono = true, inputMask = DocField.MASK_DEPARTMENT),
            f("Прописка", "г. Москва, ул. Примерная, д. 1, кв. 1", multiline = true)
        )
    )

    val zagran: DocTemplate = DocTemplate(
        typeKey = "ZAGRAN",
        title = "Загранпаспорт",
        category = DocCategory.IDENTITY,
        gradientKey = GradientKeys.SAPPHIRE,
        iconKey = IconKeys.GLOBE,
        fields = listOf(
            f("ФИО (рус.)", "Иванов Иван Иванович"),
            f("ФИО (лат.)", "IVANOV IVAN"),
            f("Серия и номер", "00 0000000", masked = true, mono = true, inputMask = DocField.MASK_ZAGRAN),
            f("Дата выдачи", "01.01.2020"),
            f("Действителен до", "01.01.2030"),
            f("Орган выдачи", "МВД 77001")
        )
    )

    val snils: DocTemplate = DocTemplate(
        typeKey = "SNILS",
        title = "СНИЛС",
        category = DocCategory.IDENTITY,
        gradientKey = GradientKeys.EMERALD,
        iconKey = IconKeys.SNILS,
        fields = listOf(
            f("Номер СНИЛС", "000-000-000 00", masked = true, mono = true, inputMask = DocField.MASK_SNILS),
            f("ФИО", "Иванов Иван Иванович"),
            f("Дата рождения", "01.01.1990")
        )
    )

    val inn: DocTemplate = DocTemplate(
        typeKey = "INN",
        title = "ИНН",
        category = DocCategory.IDENTITY,
        gradientKey = GradientKeys.GRAPHITE,
        iconKey = IconKeys.INN,
        fields = listOf(
            f("ИНН", "000000000000", masked = true, mono = true, inputMask = DocField.MASK_INN),
            f("ФИО", "Иванов Иван Иванович"),
            f("Дата постановки на учёт", "01.01.2010"),
            f("Код ФНС", "7700", mono = true)
        )
    )

    val driver: DocTemplate = DocTemplate(
        typeKey = "DRIVER",
        title = "Водительское удостоверение",
        category = DocCategory.AUTO,
        gradientKey = GradientKeys.ANTHRACITE,
        iconKey = IconKeys.DRIVER,
        fields = listOf(
            f("ФИО", "Иванов Иван Иванович"),
            f("Дата рождения", "01.01.1990"),
            f("Серия и номер", "0000 000000", masked = true, mono = true, inputMask = DocField.MASK_PASSPORT),
            f("Дата выдачи", "01.01.2020"),
            f("Действительно до", "01.01.2030"),
            f("Кем выдан", "ГИБДД 7701"),
            f("Категории", "B, C"),
            f("Стаж", "с 2010 года")
        )
    )

    val sts: DocTemplate = DocTemplate(
        typeKey = "STS",
        title = "СТС",
        category = DocCategory.AUTO,
        gradientKey = GradientKeys.ANTHRACITE,
        iconKey = IconKeys.CAR,
        fields = listOf(
            f("Госномер", "А000АА000", mono = true),
            f("VIN", "X0000000000000000", masked = true, mono = true),
            f("Марка и модель", "Lada Vesta"),
            f("Год выпуска", "2020"),
            f("Серия и номер", "00 00 000000", mono = true),
            f("Дата выдачи", "01.01.2020"),
            f("Собственник", "Иванов Иван Иванович")
        )
    )

    val pts: DocTemplate = DocTemplate(
        typeKey = "PTS",
        title = "ПТС / ЭПТС",
        category = DocCategory.AUTO,
        gradientKey = GradientKeys.ANTHRACITE,
        iconKey = IconKeys.VIN,
        fields = listOf(
            f("Номер ПТС / ЭПТС", "00 00 000000", mono = true),
            f("VIN", "X0000000000000000", masked = true, mono = true),
            f("Марка и модель", "Lada Vesta"),
            f("Двигатель", "0000000"),
            f("Кузов / шасси", "0000000"),
            f("Мощность, л.с.", "106"),
            f("Дата выдачи", "01.01.2020"),
            f("Собственник", "Иванов Иван Иванович")
        )
    )

    val osago: DocTemplate = DocTemplate(
        typeKey = "OSAGO",
        title = "ОСАГО",
        category = DocCategory.AUTO,
        gradientKey = GradientKeys.INDIGO,
        iconKey = IconKeys.SHIELD,
        fields = listOf(
            f("Страховая компания", "Страховая компания"),
            f("Серия и номер", "000 0000000", masked = true, mono = true, inputMask = DocField.MASK_POLICY),
            f("Действует с", "01.01.2024"),
            f("Действует по", "31.12.2024"),
            f("Телефон страховой", "+7 (000) 000-00-00", inputMask = DocField.MASK_PHONE)
        )
    )

    val kasko: DocTemplate = DocTemplate(
        typeKey = "KASKO",
        title = "КАСКО",
        category = DocCategory.AUTO,
        gradientKey = GradientKeys.VIOLET,
        iconKey = IconKeys.SHIELD_PLUS,
        fields = listOf(
            f("Страховая компания", "Страховая компания"),
            f("Серия и номер", "000 0000000", masked = true, mono = true, inputMask = DocField.MASK_POLICY),
            f("Действует с", "01.01.2024"),
            f("Действует по", "31.12.2024"),
            f("Телефон страховой", "+7 (000) 000-00-00", inputMask = DocField.MASK_PHONE)
        )
    )

    val oms: DocTemplate = DocTemplate(
        typeKey = "OMS",
        title = "Полис ОМС",
        category = DocCategory.MEDICINE,
        gradientKey = GradientKeys.TURQUOISE,
        iconKey = IconKeys.MEDICAL,
        fields = listOf(
            f("ЕНП (номер полиса)", "0000 0000 0000 0000", masked = true, mono = true, inputMask = DocField.MASK_OMS),
            f("Страховая компания", "Страховая медицинская компания"),
            f("ФИО", "Иванов Иван Иванович"),
            f("Дата рождения", "01.01.1990")
        )
    )

    val dms: DocTemplate = DocTemplate(
        typeKey = "DMS",
        title = "ДМС / турстраховка",
        category = DocCategory.MEDICINE,
        gradientKey = GradientKeys.TEAL,
        iconKey = IconKeys.MEDICAL_PLUS,
        fields = listOf(
            f("Страховая компания", "Страховая компания"),
            f("Номер полиса", "00000000000", masked = true, mono = true),
            f("Действует с", "01.01.2024"),
            f("Действует по", "31.12.2024"),
            f("Телефон ассистанса", "+7 (000) 000-00-00", inputMask = DocField.MASK_PHONE)
        )
    )

    val birth: DocTemplate = DocTemplate(
        typeKey = "BIRTH",
        title = "Свидетельство о рождении",
        category = DocCategory.FAMILY,
        gradientKey = GradientKeys.TERRACOTTA,
        iconKey = IconKeys.BABY,
        fields = listOf(
            f("ФИО ребёнка", "Иванов Иван Иванович"),
            f("Дата рождения", "01.01.2020"),
            f("Место рождения", "г. Москва"),
            f("Серия и номер", "00 000000", mono = true),
            f("Дата выдачи", "01.01.2020"),
            f("ЗАГС", "Московский отдел ЗАГС"),
            f("Родители", "Иванов Иван Иванович, Иванова Мария Петровна", multiline = true)
        )
    )

    val bank: DocTemplate = DocTemplate(
        typeKey = "BANK",
        title = "Банковские реквизиты",
        category = DocCategory.OTHER,
        gradientKey = GradientKeys.BRONZE,
        iconKey = IconKeys.BANK,
        fields = listOf(
            f("Банк", "Наименование банка"),
            f("БИК", "000000000", mono = true, inputMask = DocField.MASK_BIK),
            f("Расчётный счёт", "00000000000000000000", masked = true, mono = true, inputMask = DocField.MASK_ACCOUNT),
            f("Корр. счёт", "00000000000000000000", masked = true, mono = true, inputMask = DocField.MASK_ACCOUNT),
            f("ИНН банка", "0000000000", mono = true),
            f("КПП", "0000 00", mono = true, inputMask = DocField.MASK_KPP),
            f("Получатель", "Иванов Иван Иванович")
        )
    )

    val custom: DocTemplate = DocTemplate(
        typeKey = "CUSTOM",
        title = "Своя карточка",
        category = DocCategory.OTHER,
        gradientKey = GradientKeys.OBSIDIAN,
        iconKey = IconKeys.STAR,
        fields = listOf(
            f("Название карточки", "Например, пропуск на работу"),
            f("Поле 1", "Значение"),
            f("Поле 2", "Значение")
        )
    )

    val all: List<DocTemplate> = listOf(
        passport, zagran, snils, inn,
        driver, sts, pts, osago, kasko,
        oms, dms, birth, bank, custom
    )

    fun byKey(typeKey: String): DocTemplate? = all.firstOrNull { it.typeKey == typeKey }

    fun createCard(typeKey: String): DocCard? {
        val template = byKey(typeKey) ?: return null
        return DocCard(
            typeKey = template.typeKey,
            title = template.title,
            subtitle = template.category.label,
            category = template.category,
            gradientKey = template.gradientKey,
            iconKey = template.iconKey,
            fields = template.fields.map { it.copy(id = java.util.UUID.randomUUID().toString()) }
        )
    }
}
