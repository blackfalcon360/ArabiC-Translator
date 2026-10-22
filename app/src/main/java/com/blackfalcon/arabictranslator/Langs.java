package com.blackfalcon.arabictranslator;

/** The 22 Arab League countries with their usual Arabic dialect. */
public class Langs {
    // ISO, country en, ur, ar | dialect en, ur, ar | region
    public static final String[][] C = {
        {"EG", "Egypt", "مصر", "مصر", "Egyptian Arabic", "مصری عربی", "اللهجة المصرية", "0"},
        {"SD", "Sudan", "سوڈان", "السودان", "Sudanese Arabic", "سوڈانی عربی", "اللهجة السودانية", "0"},
        {"SY", "Syria", "شام (سوریہ)", "سوريا", "Syrian Arabic (Levantine)", "شامی عربی", "اللهجة السورية", "1"},
        {"LB", "Lebanon", "لبنان", "لبنان", "Lebanese Arabic (Levantine)", "لبنانی عربی", "اللهجة اللبنانية", "1"},
        {"PS", "Palestine", "فلسطین", "فلسطين", "Palestinian Arabic (Levantine)", "فلسطینی عربی", "اللهجة الفلسطينية", "1"},
        {"JO", "Jordan", "اردن", "الأردن", "Jordanian Arabic (Levantine)", "اردنی عربی", "اللهجة الأردنية", "1"},
        {"SA", "Saudi Arabia", "سعودی عرب", "السعودية", "Saudi Arabic (Najdi/Hejazi)", "سعودی عربی (نجدی/حجازی)", "اللهجة السعودية", "2"},
        {"AE", "United Arab Emirates", "متحدہ عرب امارات", "الإمارات", "Emirati Arabic (Gulf)", "اماراتی عربی", "اللهجة الإماراتية", "2"},
        {"KW", "Kuwait", "کویت", "الكويت", "Kuwaiti Arabic (Gulf)", "کویتی عربی", "اللهجة الكويتية", "2"},
        {"QA", "Qatar", "قطر", "قطر", "Qatari Arabic (Gulf)", "قطری عربی", "اللهجة القطرية", "2"},
        {"BH", "Bahrain", "بحرین", "البحرين", "Bahraini Arabic (Gulf)", "بحرینی عربی", "اللهجة البحرينية", "2"},
        {"OM", "Oman", "عمان", "عُمان", "Omani Arabic", "عمانی عربی", "اللهجة العمانية", "2"},
        {"IQ", "Iraq", "عراق", "العراق", "Iraqi Arabic", "عراقی عربی", "اللهجة العراقية", "2"},
        {"YE", "Yemen", "یمن", "اليمن", "Yemeni Arabic", "یمنی عربی", "اللهجة اليمنية", "2"},
        {"MA", "Morocco", "مراکش (مراکو)", "المغرب", "Moroccan Arabic (Darija)", "مراکشی عربی (دارجہ)", "الدارجة المغربية", "3"},
        {"DZ", "Algeria", "الجزائر", "الجزائر", "Algerian Arabic (Darja)", "الجزائری عربی", "الدارجة الجزائرية", "3"},
        {"TN", "Tunisia", "تیونس", "تونس", "Tunisian Arabic (Derja)", "تیونسی عربی", "الدارجة التونسية", "3"},
        {"LY", "Libya", "لیبیا", "ليبيا", "Libyan Arabic", "لیبیائی عربی", "اللهجة الليبية", "3"},
        {"MR", "Mauritania", "موریطانیہ", "موريتانيا", "Mauritanian Arabic (Hassaniya)", "موریطانوی عربی (حسانیہ)", "اللهجة الحسانية", "3"},
        {"SO", "Somalia", "صومالیہ", "الصومال", "Arabic as used in Somalia", "صومالیہ میں بولی جانے والی عربی", "العربية في الصومال", "4"},
        {"DJ", "Djibouti", "جبوتی", "جيبوتي", "Arabic as used in Djibouti", "جبوتی میں بولی جانے والی عربی", "العربية في جيبوتي", "4"},
        {"KM", "Comoros", "کومورو", "جزر القمر", "Arabic as used in the Comoros", "کومورو میں بولی جانے والی عربی", "العربية في جزر القمر", "4"}
    };

    public static int count() { return C.length; }

    public static String flag(String iso) {
        int a = 0x1F1E6;
        return new String(Character.toChars(a + iso.charAt(0) - 'A'))
             + new String(Character.toChars(a + iso.charAt(1) - 'A'));
    }

    public static String country(int i, String lang) {
        return C[i][lang.equals("en") ? 1 : (lang.equals("ar") ? 3 : 2)];
    }

    public static String dialect(int i, String lang) {
        return C[i][lang.equals("en") ? 4 : (lang.equals("ar") ? 6 : 5)];
    }

    public static int region(int i) { return Integer.parseInt(C[i][7]); }

    /** Lower-case code used in requests, e.g. "eg". */
    public static String code(int i) { return C[i][0].toLowerCase(java.util.Locale.ROOT); }
}
