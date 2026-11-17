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

    private fun text(
        label: String,
        placeholder: String = "",
        capitalizeWords: Boolean = false
    ): DocField = DocField(
        label = label,
        kind = FieldKind.TEXT,
        placeholder = placeholder,
        capitalizeWords = capitalizeWords
    )

    private fun multiline(label: String, placeholder: String = ""): DocField = DocField(
        label = label,
        kind = FieldKind.MULTILINE,
        multiline = true,
        placeholder = placeholder
    )

    private fun date(label: String): DocField = DocField(
        label = label,
        kind = FieldKind.DATE,
        placeholder = "дд.мм.гггг"
    )

    private fun phone(label: String): DocField = DocField(
        label = label,
        kind = FieldKind.PHONE,
        placeholder = "+7 (___) ___-__-__"
    )

    private fun maskedNumber(
        label: String,
        pattern: String,
        placeholder: String = ""
    ): DocField = DocField(
        label = label,
        kind = FieldKind.MASKED_NUMBER,
        pattern = pattern,
        masked = true,
        mono = true,
        placeholder = placeholder
    )

    private fun gender(): DocField = DocField(
        label = "Пол",
        kind = FieldKind.GENDER,
        options = listOf("Мужской", "Женский")
    )

    private fun surname(): DocField = text("Фамилия", "Иванов", capitalizeWords = true)

    private fun name(): DocField = text("Имя", "Иван", capitalizeWords = true)

    private fun patronymic(): DocField = text("Отчество (при наличии)", "Иванович", capitalizeWords = true)

    private fun nameLatin(label: String, placeholder: String): DocField = DocField(
        label = label,
        kind = FieldKind.TEXT,
        placeholder = placeholder,
        capitalizeWords = true
    )

    val passport: DocTemplate = DocTemplate(
        typeKey = "PASSPORT_RF",
        title = "Паспорт РФ",
        category = DocCategory.IDENTITY,
        gradientKey = GradientKeys.RUBY,
        iconKey = IconKeys.PASSPORT,
        fields = listOf(
            surname(),
            name(),
            patronymic(),
            gender(),
            date("Дата рождения"),
            text("Место рождения", "г. Москва", capitalizeWords = true),
            maskedNumber("Серия и номер", "0000 000000", "0000 000000"),
            text("Кем выдан", "ГУ МВД России по г. Москве"),
            date("Дата выдачи"),
            DocField(
                label = "Код подразделения",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "000-000",
                mono = true,
                placeholder = "000-000"
            ),
            multiline("Прописка", "г. Москва, ул. Примерная, д. 1, кв. 1")
        )
    )

    val zagran: DocTemplate = DocTemplate(
        typeKey = "ZAGRAN",
        title = "Загранпаспорт",
        category = DocCategory.IDENTITY,
        gradientKey = GradientKeys.SAPPHIRE,
        iconKey = IconKeys.GLOBE,
        fields = listOf(
            surname(),
            name(),
            patronymic(),
            nameLatin("ФИО латиницей", "IVANOV IVAN"),
            maskedNumber("Серия и номер", "00 0000000", "00 0000000"),
            date("Дата выдачи"),
            date("Действителен до"),
            DocField(
                label = "Орган выдачи",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "000000",
                mono = true,
                placeholder = "000000"
            )
        )
    )

    val snils: DocTemplate = DocTemplate(
        typeKey = "SNILS",
        title = "СНИЛС",
        category = DocCategory.IDENTITY,
        gradientKey = GradientKeys.EMERALD,
        iconKey = IconKeys.SNILS,
        fields = listOf(
            surname(),
            name(),
            patronymic(),
            date("Дата рождения"),
            maskedNumber("Номер СНИЛС", "000-000-000 00", "000-000-000 00")
        )
    )

    val inn: DocTemplate = DocTemplate(
        typeKey = "INN",
        title = "ИНН",
        category = DocCategory.IDENTITY,
        gradientKey = GradientKeys.GRAPHITE,
        iconKey = IconKeys.INN,
        fields = listOf(
            surname(),
            name(),
            patronymic(),
            maskedNumber("ИНН", "000000000000", "12 цифр"),
            date("Дата постановки на учёт"),
            DocField(
                label = "Код ФНС",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "0000",
                mono = true,
                placeholder = "7700"
            )
        )
    )

    val driver: DocTemplate = DocTemplate(
        typeKey = "DRIVER",
        title = "Водительское удостоверение",
        category = DocCategory.AUTO,
        gradientKey = GradientKeys.ANTHRACITE,
        iconKey = IconKeys.DRIVER,
        fields = listOf(
            surname(),
            name(),
            patronymic(),
            date("Дата рождения"),
            maskedNumber("Серия и номер", "00 00 000000", "00 00 000000"),
            date("Дата выдачи"),
            date("Действительно до"),
            text("Кем выдан", "ГИБДД 7701"),
            DocField(
                label = "Категории",
                kind = FieldKind.CHIPS,
                options = listOf(
                    "A", "A1", "B", "B1", "C", "C1", "D", "D1",
                    "BE", "CE", "DE", "M"
                )
            ),
            text("Стаж", "с 2010 года")
        )
    )

    val sts: DocTemplate = DocTemplate(
        typeKey = "STS",
        title = "СТС",
        category = DocCategory.AUTO,
        gradientKey = GradientKeys.ANTHRACITE,
        iconKey = IconKeys.CAR,
        fields = listOf(
            DocField(
                label = "Госномер",
                kind = FieldKind.TEXT,
                mono = true,
                placeholder = "А000АА000",
                capitalizeWords = true
            ),
            DocField(
                label = "VIN",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "00000000000000000",
                masked = true,
                mono = true,
                placeholder = "17 символов"
            ),
            text("Марка и модель", "Lada Vesta"),
            maskedNumber("Серия и номер", "00 00 000000", "00 00 000000"),
            text("Год выпуска", "2020"),
            date("Дата выдачи"),
            text("Собственник", "Иванов Иван Иванович", capitalizeWords = true)
        )
    )

    val pts: DocTemplate = DocTemplate(
        typeKey = "PTS",
        title = "ПТС / ЭПТС",
        category = DocCategory.AUTO,
        gradientKey = GradientKeys.ANTHRACITE,
        iconKey = IconKeys.VIN,
        fields = listOf(
            maskedNumber("Номер ПТС / ЭПТС", "00 00 000000", "00 00 000000"),
            DocField(
                label = "VIN",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "00000000000000000",
                masked = true,
                mono = true,
                placeholder = "17 символов"
            ),
            text("Марка и модель", "Lada Vesta"),
            DocField(
                label = "Двигатель",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "0000000",
                mono = true,
                placeholder = "0000000"
            ),
            DocField(
                label = "Кузов / шасси",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "0000000",
                mono = true,
                placeholder = "0000000"
            ),
            text("Мощность, л.с.", "106"),
            date("Дата выдачи"),
            text("Собственник", "Иванов Иван Иванович", capitalizeWords = true)
        )
    )

    private fun policy(typeKey: String, title: String, gradient: String, icon: String): DocTemplate =
        DocTemplate(
            typeKey = typeKey,
            title = title,
            category = DocCategory.AUTO,
            gradientKey = gradient,
            iconKey = icon,
            fields = listOf(
                text("Страховая компания", "Название страховой"),
                maskedNumber("Серия и номер", "000 0000000", "000 0000000"),
                date("Действует с"),
                date("Действует по"),
                phone("Телефон страховой")
            )
        )

    val osago: DocTemplate = policy(
        typeKey = "OSAGO",
        title = "ОСАГО",
        gradient = GradientKeys.INDIGO,
        icon = IconKeys.SHIELD
    )

    val kasko: DocTemplate = policy(
        typeKey = "KASKO",
        title = "КАСКО",
        gradient = GradientKeys.VIOLET,
        icon = IconKeys.SHIELD_PLUS
    )

    val oms: DocTemplate = DocTemplate(
        typeKey = "OMS",
        title = "Полис ОМС",
        category = DocCategory.MEDICINE,
        gradientKey = GradientKeys.TURQUOISE,
        iconKey = IconKeys.MEDICAL,
        fields = listOf(
            surname(),
            name(),
            patronymic(),
            date("Дата рождения"),
            maskedNumber("ЕНП (номер полиса)", "0000 0000 0000 0000", "16 цифр"),
            text("Страховая компания", "Страховая медицинская компания")
        )
    )

    val dms: DocTemplate = DocTemplate(
        typeKey = "DMS",
        title = "ДМС / турстраховка",
        category = DocCategory.MEDICINE,
        gradientKey = GradientKeys.TEAL,
        iconKey = IconKeys.MEDICAL_PLUS,
        fields = listOf(
            text("Страховая компания", "Страховая компания"),
            maskedNumber("Номер полиса", "00000000000", "номер полиса"),
            date("Действует с"),
            date("Действует по"),
            phone("Телефон ассистанса")
        )
    )

    val birth: DocTemplate = DocTemplate(
        typeKey = "BIRTH",
        title = "Свидетельство о рождении",
        category = DocCategory.FAMILY,
        gradientKey = GradientKeys.TERRACOTTA,
        iconKey = IconKeys.BABY,
        fields = listOf(
            surname(),
            name(),
            patronymic(),
            date("Дата рождения"),
            text("Место рождения", "г. Москва", capitalizeWords = true),
            maskedNumber("Серия и номер", "00 000000", "00 000000"),
            date("Дата выдачи"),
            text("ЗАГС", "Московский отдел ЗАГС"),
            multiline("Родители", "Иванов Иван Иванович, Иванова Мария Петровна")
        )
    )

    val bank: DocTemplate = DocTemplate(
        typeKey = "BANK",
        title = "Банковские реквизиты",
        category = DocCategory.OTHER,
        gradientKey = GradientKeys.BRONZE,
        iconKey = IconKeys.BANK,
        fields = listOf(
            text("Банк", "Наименование банка"),
            DocField(
                label = "БИК",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "000000000",
                mono = true,
                placeholder = "9 цифр"
            ),
            maskedNumber("Расчётный счёт", "0000 0000 0000 0000 0000", "20 цифр"),
            maskedNumber("Корр. счёт", "0000 0000 0000 0000 0000", "20 цифр"),
            DocField(
                label = "ИНН банка",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "0000000000",
                mono = true,
                placeholder = "10 цифр"
            ),
            DocField(
                label = "КПП",
                kind = FieldKind.MASKED_NUMBER,
                pattern = "0000 00",
                mono = true,
                placeholder = "0000 00"
            ),
            text("Получатель", "Иванов Иван Иванович", capitalizeWords = true)
        )
    )

    val custom: DocTemplate = DocTemplate(
        typeKey = "CUSTOM",
        title = "Своя карточка",
        category = DocCategory.OTHER,
        gradientKey = GradientKeys.OBSIDIAN,
        iconKey = IconKeys.STAR,
        fields = listOf(
            DocField(
                label = "Поле 1",
                kind = FieldKind.TEXT,
                placeholder = "Значение",
                editableLabel = true
            ),
            DocField(
                label = "Поле 2",
                kind = FieldKind.TEXT,
                placeholder = "Значение",
                editableLabel = true
            )
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