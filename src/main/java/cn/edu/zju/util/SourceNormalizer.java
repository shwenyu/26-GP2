package cn.edu.zju.util;

import java.util.Locale;

public final class SourceNormalizer {

    private SourceNormalizer() {
    }

    public static String normalize(String source) {
        if (source == null || source.trim().isEmpty()) {
            return "unknown";
        }
        String compact = source.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();

        if (compact.equals("cpic") || compact.equals("clinical pharmacogenetics implementation consortium")) {
            return "cpic";
        }
        if (compact.equals("cpnds") || compact.equals("canadian pharmacogenomics network for drug safety")) {
            return "cpnds";
        }
        if (compact.equals("dpwg") || compact.equals("dutch pharmacogenetics working group")) {
            return "dpwg";
        }
        if (compact.equals("fda")
                || compact.equals("food and drug administration")
                || compact.equals("u s food and drug administration")
                || compact.equals("us food and drug administration")) {
            return "fda";
        }
        if (compact.equals("pro") || compact.equals("professional society")) {
            return "pro";
        }
        if (compact.equals("pharmgkb") || compact.contains("pharmgkb")) {
            return "pharmgkb";
        }
        return compact;
    }
}

