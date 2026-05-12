package com.example.market;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BrandData {

    // Структура: Марка → Модель → Кузова
    private static Map<String, Map<String, List<String>>> brandHierarchy;

    static {
        brandHierarchy = new LinkedHashMap<>();

        // ==================== BMW ====================
        Map<String, List<String>> bmwModels = new LinkedHashMap<>();
        bmwModels.put("1 Series", List.of("E81 (2004-2013)", "E82 (2004-2013)", "E87 (2004-2011)", "F20 (2011-2019)", "F40 (2019-2024)"));
        bmwModels.put("3 Series", List.of("E46 (1998-2006)", "E90 (2005-2013)", "F30 (2011-2019)", "G20 (2018-2024)"));
        bmwModels.put("5 Series", List.of("E60 (2003-2010)", "F10 (2009-2017)", "G30 (2016-2023)", "G60 (2023-2026)"));
        bmwModels.put("7 Series", List.of("E65 (2001-2008)", "F01 (2008-2015)", "G11 (2015-2022)", "G70 (2022-2026)"));
        bmwModels.put("X1", List.of("E84 (2009-2015)", "F48 (2015-2022)", "U11 (2022-н.в.)"));
        bmwModels.put("X3", List.of("E83 (2003-2010)", "F25 (2010-2017)", "G01 (2017-2024)", "G45 (2024-н.в.)"));
        bmwModels.put("X5", List.of("E53 (1999-2006)", "E70 (2006-2013)", "F15 (2013-2018)", "G05 (2018-2023)"));
        bmwModels.put("X6", List.of("E71 (2007-2014)", "F16 (2014-2019)", "G06 (2019-2023)"));
        bmwModels.put("M3", List.of("E90 (2007-2013)", "F80 (2014-2018)", "G80 (2020-2024)"));
        bmwModels.put("M5", List.of("F10 (2011-2016)", "F90 (2017-2023)", "G90 (2024-н.в.)"));
        bmwModels.put("Z4", List.of("E85 (2002-2008)", "E89 (2009-2016)", "G29 (2018-2024)"));
        brandHierarchy.put("BMW", bmwModels);

        // ==================== Mercedes-Benz ====================
        Map<String, List<String>> mbModels = new LinkedHashMap<>();
        mbModels.put("A-Class", List.of("W176 (2012-2018)", "W177 (2018-2024)"));
        mbModels.put("C-Class", List.of("W204 (2007-2014)", "W205 (2014-2021)", "W206 (2021-2024)"));
        mbModels.put("E-Class", List.of("W212 (2009-2016)", "W213 (2016-2023)", "W214 (2023-2024)"));
        mbModels.put("S-Class", List.of("W221 (2005-2013)", "W222 (2013-2020)", "W223 (2020-2024)"));
        mbModels.put("GLA", List.of("X156 (2013-2020)", "H247 (2020-2024)"));
        mbModels.put("GLC", List.of("X253 (2015-2022)", "X254 (2022-2024)"));
        mbModels.put("GLE", List.of("W166 (2011-2018)", "W167 (2018-2023)"));
        mbModels.put("G-Class", List.of("W463 (1990-2024)"));
        mbModels.put("AMG GT", List.of("C192 (2014-2024)"));
        brandHierarchy.put("Mercedes-Benz", mbModels);

        // ==================== Audi ====================
        Map<String, List<String>> audiModels = new LinkedHashMap<>();
        audiModels.put("A3", List.of("8P (2003-2012)", "8V (2012-2020)", "8Y (2020-2024)"));
        audiModels.put("A4", List.of("B8 (2007-2015)", "B9 (2015-2023)"));
        audiModels.put("A6", List.of("C7 (2011-2018)", "C8 (2018-2024)"));
        audiModels.put("Q3", List.of("8U (2011-2018)", "F3 (2018-2024)"));
        audiModels.put("Q5", List.of("8R (2008-2017)", "FY (2017-2024)"));
        audiModels.put("Q7", List.of("4L (2005-2015)", "4M (2015-2024)"));
        audiModels.put("Q8", List.of("4MN (2018-2024)"));
        audiModels.put("TT", List.of("8J (2006-2014)", "FV (2014-2023)"));
        audiModels.put("R8", List.of("42 (2006-2015)", "4S (2015-2024)"));
        brandHierarchy.put("Audi", audiModels);

        // ==================== Toyota ====================
        Map<String, List<String>> toyotaModels = new LinkedHashMap<>();
        toyotaModels.put("Corolla", List.of("E150 (2006-2013)", "E170 (2013-2018)", "E210 (2018-2024)"));
        toyotaModels.put("Camry", List.of("XV40 (2006-2011)", "XV50 (2011-2017)", "XV70 (2017-2024)"));
        toyotaModels.put("RAV4", List.of("XA30 (2005-2013)", "XA40 (2013-2018)", "XA50 (2018-2024)"));
        toyotaModels.put("Land Cruiser", List.of("J100 (1998-2007)", "J200 (2007-2021)", "J300 (2021-2024)"));
        toyotaModels.put("Land Cruiser Prado", List.of("J120 (2002-2009)", "J150 (2009-2023)", "J250 (2023-2024)"));
        toyotaModels.put("Hilux", List.of("AN10 (2004-2015)", "AN120 (2015-2024)"));
        toyotaModels.put("Supra", List.of("A80 (1993-2002)", "A90 (2019-2024)"));
        brandHierarchy.put("Toyota", toyotaModels);

        // ==================== Kia ====================
        Map<String, List<String>> kiaModels = new LinkedHashMap<>();
        kiaModels.put("Rio", List.of("UB (2011-2017)", "YB (2017-2024)"));
        kiaModels.put("Sportage", List.of("SL (2010-2016)", "QL (2016-2021)", "NQ5 (2021-2024)"));
        kiaModels.put("Sorento", List.of("XM (2009-2014)", "UM (2014-2020)", "MQ4 (2020-2024)"));
        kiaModels.put("Cerato", List.of("YD (2013-2018)", "BD (2018-2024)"));
        brandHierarchy.put("Kia", kiaModels);

        // ==================== Hyundai ====================
        Map<String, List<String>> hyundaiModels = new LinkedHashMap<>();
        hyundaiModels.put("Solaris", List.of("RB (2010-2017)", "HC (2017-2024)"));
        hyundaiModels.put("Tucson", List.of("LM (2009-2015)", "TL (2015-2020)", "NX4 (2020-2024)"));
        hyundaiModels.put("Santa Fe", List.of("DM (2012-2018)", "TM (2018-2023)", "MX5 (2023-2024)"));
        brandHierarchy.put("Hyundai", hyundaiModels);

        // ==================== Volkswagen ====================
        Map<String, List<String>> vwModels = new LinkedHashMap<>();
        vwModels.put("Golf", List.of("5K (2008-2012)", "5G (2012-2020)", "CD (2019-2024)"));
        vwModels.put("Passat", List.of("B6 (2005-2010)", "B7 (2010-2015)", "B8 (2014-2022)", "B9 (2024-н.в.)"));
        vwModels.put("Tiguan", List.of("5N (2007-2016)", "AD (2016-2024)"));
        vwModels.put("Touareg", List.of("7P (2010-2018)", "CR (2018-2024)"));
        brandHierarchy.put("Volkswagen", vwModels);

        // ==================== Lada ====================
        Map<String, List<String>> ladaModels = new LinkedHashMap<>();
        ladaModels.put("Granta", List.of("2190 (2011-2024)"));
        ladaModels.put("Vesta", List.of("NG (2015-2024)"));
        ladaModels.put("Niva Legend", List.of("2121 (1977-2024)"));
        ladaModels.put("Niva Travel", List.of("2123 (2020-2024)"));
        ladaModels.put("Largus", List.of("R90 (2012-2024)"));
        ladaModels.put("Priora", List.of("2170/2171/2172 (2007-2018)"));
        brandHierarchy.put("Lada (ВАЗ)", ladaModels);
    }

    // Получить все марки
    public static List<String> getAllBrands() {
        List<String> brands = new ArrayList<>(brandHierarchy.keySet());
        java.util.Collections.sort(brands);
        return brands;
    }

    // Получить модели для марки
    public static List<String> getModelNames(String brand) {
        Map<String, List<String>> models = brandHierarchy.get(brand);
        if (models == null) return new ArrayList<>();
        List<String> names = new ArrayList<>(models.keySet());
        java.util.Collections.sort(names);
        return names;
    }

    // Получить кузова для модели
    public static List<String> getBodiesForModel(String brand, String model) {
        Map<String, List<String>> models = brandHierarchy.get(brand);
        if (models == null) return new ArrayList<>();
        List<String> bodies = models.get(model);
        return bodies != null ? new ArrayList<>(bodies) : new ArrayList<>();
    }

    // Старый метод для совместимости
    public static Map<String, List<String>> getBrandsWithModels() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String brand : brandHierarchy.keySet()) {
            List<String> allItems = new ArrayList<>();
            Map<String, List<String>> models = brandHierarchy.get(brand);
            for (String model : models.keySet()) {
                for (String body : models.get(model)) {
                    allItems.add(model + " → " + body);
                }
            }
            result.put(brand, allItems);
        }
        return result;
    }

    public static List<String> getModelsForBrand(String brand) {
        return getBrandsWithModels().get(brand) != null ?
                new ArrayList<>(getBrandsWithModels().get(brand)) : new ArrayList<>();
    }
}