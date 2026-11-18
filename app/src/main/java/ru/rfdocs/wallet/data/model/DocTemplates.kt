package ru.rfdocs.wallet.data.model

import kotlinx.serialization.Serializable
import ru.rfdocs.wallet.ui.input.Masks

@Serializable
data class FieldSpec(
    val key: String,
    val label: String,
    val mask: String? = null,
    val secret: Boolean = false,
    val phone: Boolean = false
)

@Serializable
data class DocTemplate(
    val key: String,
    val title: String,
    val category: DocCategory,
    val fields: List<FieldSpec>
)

object DocTemplates {

    private fun text(key: String, label: String): FieldSpec =
        FieldSpec(key = key, label = label)

    private fun date(key: String, label: String): FieldSpec =
        FieldSpec(key = key, label = label, mask = Masks.DATE)

    private fun phone(key: String, label: String): FieldSpec =
        FieldSpec(key = key, label = label, mask = Masks.PHONE, phone = true)

    private fun masked(
        key: String,
        label: String,
        mask: String
    ): FieldSpec = FieldSpec(key = key, label = label, mask = mask, secret = true)

    private val surname = text("surname", "Фамилия")
    private val name = text("name", "Имя")
    private val patronymic = text("patronymic", "Отчество (при наличии)")

    val passport: DocTemplate = DocTemplate(
        key = "PASSPORT_RF",
        title = "Паспорт РФ",
        category = DocCategory.IDENTITY,
        fields = listOf(
            surname,
            name,
            patronymic,
            text("gender", "Пол"),
            date("birth_date", "Дата рождения"),
            text("birth_place", "Место рождения"),
            masked("number", "Серия и номер", Masks.PASSPORT),
            text("issued_by", "Кем выдан"),
            date("issue_date", "Дата выдачи"),
            FieldSpec("department", "Код подразделения", mask = Masks.DEPARTMENT),
            text("address", "Адрес регистрации")
        )
    )

    val zagran: DocTemplate = DocTemplate(
        key = "ZAGRAN",
        title = "Загранпаспорт",
        category = DocCategory.IDENTITY,
        fields = listOf(
            surname,
            name,
            text("latin_name", "ФИО латиницей"),
            masked("number", "Серия и номер", "00 0000000"),
            date("issue_date", "Дата выдачи"),
            date("valid_until", "Действителен до"),
            text("issued_by", "Орган выдачи")
        )
    )

    val snils: DocTemplate = DocTemplate(
        key = "SNILS",
        title = "СНИЛС",
        category = DocCategory.IDENTITY,
        fields = listOf(
            surname,
            name,
            patronymic,
            masked("number", "Номер СНИЛС", Masks.SNILS),
            date("birth_date", "Дата рождения")
        )
    )

    val inn: DocTemplate = DocTemplate(
        key = "INN",
        title = "ИНН",
        category = DocCategory.IDENTITY,
        fields = listOf(
            surname,
            name,
            patronymic,
            masked("number", "ИНН", Masks.INN),
            date("issue_date", "Дата постановки на учёт"),
            FieldSpec("fns_code", "Код ФНС", mask = "0000")
        )
    )

    val driver: DocTemplate = DocTemplate(
        key = "DRIVER",
        title = "Водительское удостоверение",
        category = DocCategory.AUTO,
        fields = listOf(
            surname,
            name,
            patronymic,
            date("birth_date", "Дата рождения"),
            masked("number", "Серия и номер", Masks.VEHICLE),
            date("issue_date", "Дата выдачи"),
            date("valid_until", "Действителен до"),
            text("issued_by", "Кем выдан"),
            text("categories", "Категории"),
            text("experience", "Стаж")
        )
    )

    val sts: DocTemplate = DocTemplate(
        key = "STS",
        title = "СТС",
        category = DocCategory.AUTO,
        fields = listOf(
            text("plate", "Госномер"),
            masked("vin", "VIN", Masks.VEHICLE),
            text("model", "Марка и модель"),
            text("year", "Год выпуска"),
            masked("number", "Серия и номер", Masks.VEHICLE),
            date("issue_date", "Дата выдачи"),
            text("owner", "Собственник")
        )
    )

    val pts: DocTemplate = DocTemplate(
        key = "PTS",
        title = "ПТС / ЭПТС",
        category = DocCategory.AUTO,
        fields = listOf(
            masked("number", "Номер ПТС / ЭПТС", Masks.VEHICLE),
            masked("vin", "VIN", Masks.VEHICLE),
            text("model", "Марка и модель"),
            text("engine", "Двигатель"),
            text("power", "Мощность, л.с."),
            date("issue_date", "Дата выдачи"),
            text("owner", "Собственник")
        )
    )

    val osago: DocTemplate = DocTemplate(
        key = "OSAGO",
        title = "ОСАГО",
        category = DocCategory.AUTO,
        fields = listOf(
            text("insurer", "Страховая компания"),
            masked("number", "Серия и номер", "000 0000000"),
            date("valid_from", "Действует с"),
            date("valid_until", "Действует по"),
            phone("insurer_phone", "Телефон страховой")
        )
    )

    val kasko: DocTemplate = DocTemplate(
        key = "KASKO",
        title = "КАСКО",
        category = DocCategory.AUTO,
        fields = listOf(
            text("insurer", "Страховая компания"),
            masked("number", "Серия и номер", "000 0000000"),
            date("valid_from", "Действует с"),
            date("valid_until", "Действует по"),
            phone("insurer_phone", "Телефон страховой")
        )
    )

    val oms: DocTemplate = DocTemplate(
        key = "OMS",
        title = "Полис ОМС",
        category = DocCategory.MEDICINE,
        fields = listOf(
            surname,
            name,
            patronymic,
            date("birth_date", "Дата рождения"),
            masked("number", "ЕНП (номер полиса)", Masks.OMS),
            text("insurer", "Страховая компания")
        )
    )

    val dms: DocTemplate = DocTemplate(
        key = "DMS",
        title = "ДМС / турстраховка",
        category = DocCategory.MEDICINE,
        fields = listOf(
            text("insurer", "Страховая компания"),
            masked("number", "Номер полиса", "00000000000"),
            date("valid_from", "Действует с"),
            date("valid_until", "Действует по"),
            phone("assistance_phone", "Телефон ассистанса")
        )
    )

    val birth: DocTemplate = DocTemplate(
        key = "BIRTH",
        title = "Свидетельство о рождении",
        category = DocCategory.FAMILY,
        fields = listOf(
            surname,
            name,
            patronymic,
            date("birth_date", "Дата рождения"),
            text("birth_place", "Место рождения"),
            masked("number", "Серия и номер", "00 000000"),
            date("issue_date", "Дата выдачи"),
            text("registry_office", "ЗАГС"),
            text("parents", "Родители")
        )
    )

    val bank: DocTemplate = DocTemplate(
        key = "BANK",
        title = "Банковские реквизиты",
        category = DocCategory.OTHER,
        fields = listOf(
            text("bank", "Банк"),
            FieldSpec("bik", "БИК", mask = "000000000"),
            masked("account", "Расчётный счёт", Masks.ACCOUNT),
            masked("corr_account", "Корр. счёт", Masks.ACCOUNT),
            FieldSpec("inn", "ИНН", mask = Masks.INN),
            FieldSpec("kpp", "КПП", mask = "000000000"),
            text("recipient", "Получатель")
        )
    )

    val custom: DocTemplate = DocTemplate(
        key = "CUSTOM",
        title = "Своя карточка",
        category = DocCategory.OTHER,
        fields = emptyList()
    )

    val all: List<DocTemplate> = listOf(
        passport, zagran, snils, inn,
        driver, sts, pts, osago, kasko,
        oms, dms, birth, bank, custom
    )

    fun byKey(typeKey: String): DocTemplate? {
        if (typeKey.isBlank()) return null
        val normalized = typeKey.lowercase()
        all.firstOrNull { it.key.equals(typeKey, ignoreCase = true) }?.let { return it }

        return when {
            normalized.contains("foreign") || normalized.contains("zagran") -> zagran
            normalized.contains("passport") -> passport
            normalized.contains("snils") -> snils
            normalized.contains("inn") -> inn
            normalized.contains("driver") || normalized.contains("vu") -> driver
            normalized.contains("sts") -> sts
            normalized.contains("pts") -> pts
            normalized.contains("osago") -> osago
            normalized.contains("kasko") -> kasko
            normalized.contains("oms") -> oms
            normalized.contains("dms") -> dms
            normalized.contains("birth") -> birth
            normalized.contains("bank") -> bank
            normalized.contains("custom") -> custom
            else -> null
        }
    }

    fun keyOf(typeKey: String): String = byKey(typeKey)?.key ?: typeKey

    fun titleOf(typeKey: String): String = byKey(typeKey)?.title ?: "Документ"
}