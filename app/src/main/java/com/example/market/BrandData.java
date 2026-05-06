package com.example.market;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BrandData {

    public static Map<String, List<String>> getBrandsWithModels() {
        Map<String, List<String>> brands = new LinkedHashMap<>();

        brands.put("BMW", Arrays.asList(
                "1 Series (F70) - Хэтчбек 2004-2024",
                "2 Series Gran Coupe (F74) - Седан 2020-2024",
                "3 Series (G20) - Седан 2018-2024",
                "3 Series Touring (G21) - Универсал 2018-2024",
                "4 Series Gran Coupe (G26) - Лифтбек 2020-2024",
                "5 Series (G60) - Седан 2023-2026",
                "5 Series Touring (G61) - Универсал 2023-2026",
                "7 Series (G70) - Седан 2022-2026",
                "X1 (U11) - Кроссовер 2022-н.в.",
                "X2 (U10) - Кроссовер 2023-н.в.",
                "X3 (G45) - Кроссовер 2024-н.в.",
                "X4 (G02) - Кроссовер-купе 2018-2024",
                "X5 (G05) - Кроссовер 2019-2023",
                "X6 (G06) - Кроссовер-купе 2020-2023",
                "X7 (G07) - Кроссовер 2018-2022",
                "i3 - Седан 2022-н.в.",
                "i4 (G26 BEV) - Лифтбек 2021-2024",
                "i5 (G60 BEV) - Седан 2023-н.в.",
                "i7 (G70 BEV) - Седан 2022-2026",
                "iX1 (U11 BEV) - Кроссовер 2022-н.в.",
                "iX3 (NA5) - Кроссовер 2025-н.в.",
                "iX (I20) - Кроссовер 2021-2025",
                "M2 (G87) - Купе 2022-н.в.",
                "M3 (G80) - Седан 2020-2024",
                "M4 (G82) - Купе 2020-2024",
                "M5 (G90) - Седан 2024-н.в.",
                "M8 (F92) - Купе 2019-2024",
                "Z4 (G29) - Родстер 2018-2024",
                "2 Series Active Tourer (U06) - Минивэн 2021-н.в.",
                "XM (G09) - Кроссовер 2022-н.в."
        ));

        brands.put("Mercedes-Benz", Arrays.asList(
                "A-Class (W177) - Хэтчбек 2018-2024",
                "A-Class Sedan (V177) - Седан 2018-2024",
                "B-Class (W247) - Минивэн 2019-2024",
                "C-Class (W206) - Седан 2021-2024",
                "C-Class Estate (S206) - Универсал 2021-2024",
                "E-Class (W214) - Седан 2023-2024",
                "E-Class Estate (S214) - Универсал 2023-2024",
                "S-Class (W223) - Седан 2020-2024",
                "CLA (C118) - Купе-седан 2019-2024",
                "CLS (C257) - Купе-седан 2018-2023",
                "GLA (H247) - Кроссовер 2020-2024",
                "GLB (X247) - Кроссовер 2019-2024",
                "GLC (X254) - Кроссовер 2022-2024",
                "GLE (W167) - Кроссовер 2019-2023",
                "GLS (X167) - Кроссовер 2019-2023",
                "G-Class (W463) - Внедорожник 2018-2024",
                "EQS (V297) - Седан 2021-2024",
                "EQE (V295) - Седан 2022-2024",
                "EQB (X243) - Кроссовер 2021-2024",
                "AMG GT (C192) - Купе 2018-2024",
                "SL (R232) - Родстер 2021-2024"
        ));

        brands.put("Audi", Arrays.asList(
                "A3 (8Y) - Хэтчбек 2020-2024",
                "A3 Sedan (8Y) - Седан 2020-2024",
                "A4 (B9) - Седан 2015-2023",
                "A5 (F5) - Купе 2016-2024",
                "A6 (C8) - Седан 2018-2024",
                "A7 (C8) - Лифтбек 2018-2024",
                "A8 (D5) - Седан 2017-2024",
                "Q2 - Кроссовер 2016-2024",
                "Q3 (F3) - Кроссовер 2018-2024",
                "Q5 (FY) - Кроссовер 2017-2024",
                "Q7 (4M) - Кроссовер 2015-2024",
                "Q8 - Кроссовер-купе 2019-2024",
                "e-tron - Кроссовер 2019-2024",
                "e-tron GT - Седан 2021-2024",
                "RS3 (8Y) - Хэтчбек 2021-2024",
                "RS6 Avant (C8) - Универсал 2019-2024",
                "RS7 (C8) - Лифтбек 2019-2024",
                "TT (FV) - Купе 2014-2023",
                "R8 (4S) - Купе 2015-2024"
        ));

        brands.put("Toyota", Arrays.asList(
                "Corolla (E210) - Седан 2018-2024",
                "Camry (XV70) - Седан 2017-2024",
                "Yaris (XP210) - Хэтчбек 2020-2024",
                "Prius (XW60) - Хэтчбек 2022-2024",
                "RAV4 (XA50) - Кроссовер 2018-2024",
                "Highlander (XU70) - Кроссовер 2019-2024",
                "Land Cruiser 300 - Внедорожник 2021-2024",
                "Land Cruiser Prado - Внедорожник 2023-2024",
                "Hilux - Пикап 2015-2024",
                "C-HR - Кроссовер 2016-2024",
                "Supra (A90) - Купе 2019-2024",
                "GR Yaris - Хэтчбек 2020-2024"
        ));

        brands.put("Kia", Arrays.asList(
                "Rio - Хэтчбек 2017-2024",
                "Cerato - Седан 2018-2024",
                "K5 - Седан 2019-2024",
                "Stinger - Лифтбек 2017-2023",
                "Sportage (NQ5) - Кроссовер 2021-2024",
                "Sorento (MQ4) - Кроссовер 2020-2024",
                "Seltos - Кроссовер 2019-2024",
                "Soul - Хэтчбек 2019-2024",
                "EV6 - Кроссовер 2021-2024",
                "Carnival - Минивэн 2020-2024",
                "Ceed - Хэтчбек 2018-2024"
        ));

        brands.put("Hyundai", Arrays.asList(
                "Solaris - Седан 2017-2024",
                "Elantra - Седан 2020-2024",
                "Sonata - Седан 2019-2024",
                "Creta - Кроссовер 2019-2024",
                "Tucson (NX4) - Кроссовер 2020-2024",
                "Santa Fe - Кроссовер 2023-2024",
                "Palisade - Кроссовер 2018-2024",
                "i30 - Хэтчбек 2016-2024",
                "Ioniq 5 - Кроссовер 2021-2024",
                "Kona - Кроссовер 2023-2024",
                "Staria - Минивэн 2021-2024"
        ));

        brands.put("Volkswagen", Arrays.asList(
                "Polo - Хэтчбек 2017-2024",
                "Golf - Хэтчбек 2019-2024",
                "Passat - Седан 2024-н.в.",
                "Jetta - Седан 2018-2024",
                "Tiguan - Кроссовер 2016-2024",
                "Touareg - Кроссовер 2018-2024",
                "Teramont - Кроссовер 2017-2024",
                "ID.3 - Хэтчбек 2019-2024",
                "ID.4 - Кроссовер 2020-2024",
                "Multivan - Минивэн 2021-2024"
        ));

        brands.put("Lada", Arrays.asList(
                "Granta - Седан 2011-2024",
                "Vesta - Седан 2015-2024",
                "Vesta SW - Универсал 2017-2024",
                "Niva Legend - Внедорожник 1977-2024",
                "Niva Travel - Внедорожник 2020-2024",
                "Largus - Универсал 2012-2024",
                "XRAY - Хэтчбек 2015-2022"
        ));

        brands.put("Renault", Arrays.asList(
                "Logan - Седан 2020-2024",
                "Sandero - Хэтчбек 2020-2024",
                "Duster - Кроссовер 2017-2024",
                "Arkana - Кроссовер-купе 2019-2024",
                "Kaptur - Кроссовер 2016-2024",
                "Megane - Хэтчбек 2016-2024"
        ));

        brands.put("Nissan", Arrays.asList(
                "Almera - Седан 2019-2024",
                "Sentra - Седан 2019-2024",
                "Qashqai - Кроссовер 2021-2024",
                "X-Trail - Кроссовер 2022-2024",
                "Murano - Кроссовер 2015-2024",
                "Juke - Кроссовер 2019-2024",
                "GT-R - Купе 2007-2024"
        ));

        brands.put("Honda", Arrays.asList(
                "Civic - Седан 2021-2024",
                "Accord - Седан 2017-2022",
                "CR-V - Кроссовер 2017-2024",
                "HR-V - Кроссовер 2021-2024",
                "Pilot - Кроссовер 2016-2024"
        ));

        brands.put("Mazda", Arrays.asList(
                "Mazda3 - Хэтчбек 2019-2024",
                "Mazda6 - Седан 2012-2024",
                "CX-5 - Кроссовер 2017-2024",
                "CX-9 - Кроссовер 2016-2024",
                "CX-30 - Кроссовер 2019-2024",
                "MX-5 - Родстер 2015-2024"
        ));

        brands.put("Lexus", Arrays.asList(
                "ES - Седан 2018-2024",
                "LS - Седан 2017-2024",
                "NX - Кроссовер 2021-2024",
                "RX - Кроссовер 2015-2024",
                "LX - Внедорожник 2007-2024"
        ));

        brands.put("Subaru", Arrays.asList(
                "Impreza - Хэтчбек 2016-2024",
                "Legacy - Седан 2014-2024",
                "Forester - Кроссовер 2018-2024",
                "Outback - Универсал 2014-2024",
                "WRX - Седан 2021-2024",
                "BRZ - Купе 2012-2024"
        ));

        brands.put("Mitsubishi", Arrays.asList(
                "Lancer - Седан 2007-2020",
                "Outlander - Кроссовер 2012-2024",
                "ASX - Кроссовер 2010-2024",
                "Pajero Sport - Внедорожник 2015-2024",
                "L200 - Пикап 2015-2024"
        ));

        brands.put("Ford", Arrays.asList(
                "Focus - Хэтчбек 2018-2024",
                "Mondeo - Седан 2014-2022",
                "Mustang - Купе 2015-2024",
                "Explorer - Кроссовер 2019-2024",
                "Kuga - Кроссовер 2019-2024",
                "Ranger - Пикап 2011-2024"
        ));

        brands.put("Chevrolet", Arrays.asList(
                "Spark - Хэтчбек 2015-2024",
                "Cruze - Седан 2016-2024",
                "Camaro - Купе 2016-2024",
                "Trax - Кроссовер 2013-2024",
                "Equinox - Кроссовер 2017-2024",
                "Tahoe - Внедорожник 2015-2024"
        ));

        brands.put("Porsche", Arrays.asList(
                "911 - Купе 2019-2024",
                "Taycan - Седан 2019-2024",
                "Panamera - Лифтбек 2016-2024",
                "Cayenne - Кроссовер 2017-2024",
                "Macan - Кроссовер 2014-2024"
        ));

        brands.put("Tesla", Arrays.asList(
                "Model 3 - Седан 2017-2024",
                "Model Y - Кроссовер 2020-2024",
                "Model S - Лифтбек 2012-2024",
                "Model X - Кроссовер 2015-2024"
        ));

        brands.put("Volvo", Arrays.asList(
                "S60 - Седан 2018-2024",
                "S90 - Седан 2016-2024",
                "XC40 - Кроссовер 2017-2024",
                "XC60 - Кроссовер 2017-2024",
                "XC90 - Кроссовер 2015-2024"
        ));

        brands.put("Land Rover", Arrays.asList(
                "Discovery - Кроссовер 2017-2024",
                "Defender - Внедорожник 2020-2024",
                "Range Rover - Кроссовер 2013-2024",
                "Range Rover Sport - Кроссовер 2013-2024",
                "Range Rover Velar - Кроссовер 2017-2024",
                "Range Rover Evoque - Кроссовер 2011-2024"
        ));

        brands.put("Jaguar", Arrays.asList(
                "XE - Седан 2015-2024",
                "XF - Седан 2015-2024",
                "F-Pace - Кроссовер 2016-2024",
                "E-Pace - Кроссовер 2017-2024",
                "F-Type - Купе 2013-2024"
        ));

        brands.put("Infiniti", Arrays.asList(
                "Q50 - Седан 2013-2024",
                "Q60 - Купе 2016-2022",
                "QX50 - Кроссовер 2017-2024",
                "QX60 - Кроссовер 2012-2024",
                "QX80 - Внедорожник 2010-2024"
        ));

        brands.put("Genesis", Arrays.asList(
                "G70 - Седан 2017-2024",
                "G80 - Седан 2020-2024",
                "G90 - Седан 2021-2024",
                "GV70 - Кроссовер 2020-2024",
                "GV80 - Кроссовер 2020-2024"
        ));

        brands.put("Skoda", Arrays.asList(
                "Octavia - Лифтбек 2020-2024",
                "Superb - Лифтбек 2015-2024",
                "Kodiaq - Кроссовер 2016-2024",
                "Karoq - Кроссовер 2017-2024"
        ));

        brands.put("Peugeot", Arrays.asList(
                "308 - Хэтчбек 2021-2024",
                "408 - Седан 2022-2024",
                "2008 - Кроссовер 2019-2024",
                "3008 - Кроссовер 2016-2024",
                "5008 - Кроссовер 2017-2024"
        ));

        brands.put("Citroen", Arrays.asList(
                "C3 - Хэтчбек 2016-2024",
                "C4 - Хэтчбек 2020-2024",
                "C5 Aircross - Кроссовер 2018-2024"
        ));

        brands.put("Opel", Arrays.asList(
                "Corsa - Хэтчбек 2019-2024",
                "Astra - Хэтчбек 2015-2024",
                "Grandland - Кроссовер 2017-2024",
                "Mokka - Кроссовер 2020-2024"
        ));

        brands.put("Chery", Arrays.asList(
                "Tiggo 4 - Кроссовер 2017-2024",
                "Tiggo 7 - Кроссовер 2016-2024",
                "Tiggo 8 - Кроссовер 2018-2024"
        ));

        brands.put("Haval", Arrays.asList(
                "Jolion - Кроссовер 2020-2024",
                "F7 - Кроссовер 2018-2024",
                "Dargo - Кроссовер 2021-2024",
                "H9 - Внедорожник 2014-2024"
        ));

        brands.put("Geely", Arrays.asList(
                "Atlas - Кроссовер 2016-2024",
                "Coolray - Кроссовер 2018-2024",
                "Tugella - Кроссовер-купе 2019-2024",
                "Monjaro - Кроссовер 2021-2024"
        ));

        brands.put("Changan", Arrays.asList(
                "Alsvin - Седан 2017-2024",
                "CS35 - Кроссовер 2012-2024",
                "CS55 - Кроссовер 2017-2024",
                "CS75 - Кроссовер 2014-2024",
                "Uni-K - Кроссовер 2021-2024"
        ));

        brands.put("Dodge", Arrays.asList(
                "Challenger - Купе 2008-2023",
                "Charger - Седан 2005-2023",
                "Durango - Кроссовер 2010-2024"
        ));

        brands.put("Jeep", Arrays.asList(
                "Renegade - Кроссовер 2014-2024",
                "Compass - Кроссовер 2016-2024",
                "Cherokee - Кроссовер 2013-2024",
                "Grand Cherokee - Кроссовер 2010-2024",
                "Wrangler - Внедорожник 2007-2024"
        ));

        brands.put("Mini", Arrays.asList(
                "Cooper - Хэтчбек 2014-2024",
                "Countryman - Кроссовер 2017-2024",
                "Clubman - Универсал 2015-2024"
        ));

        brands.put("Ferrari", Arrays.asList(
                "Roma - Купе 2020-2024",
                "F8 Tributo - Купе 2019-2024",
                "SF90 Stradale - Купе 2019-2024",
                "Purosangue - Кроссовер 2022-2024"
        ));

        brands.put("Lamborghini", Arrays.asList(
                "Huracan - Купе 2014-2024",
                "Aventador - Купе 2011-2022",
                "Urus - Кроссовер 2018-2024",
                "Revuelto - Купе 2023-2024"
        ));

        brands.put("Bentley", Arrays.asList(
                "Bentayga - Кроссовер 2015-2024",
                "Continental GT - Купе 2017-2024",
                "Flying Spur - Седан 2019-2024"
        ));

        brands.put("Rolls-Royce", Arrays.asList(
                "Phantom - Седан 2017-2024",
                "Ghost - Седан 2020-2024",
                "Cullinan - Кроссовер 2018-2024",
                "Spectre - Купе 2023-2024"
        ));

        brands.put("Aston Martin", Arrays.asList(
                "Vantage - Купе 2018-2024",
                "DB11 - Купе 2016-2024",
                "DBX - Кроссовер 2020-2024"
        ));

        brands.put("Maserati", Arrays.asList(
                "Ghibli - Седан 2013-2024",
                "Quattroporte - Седан 2013-2024",
                "Levante - Кроссовер 2016-2024",
                "Grecale - Кроссовер 2022-2024"
        ));

        brands.put("Cadillac", Arrays.asList(
                "CT4 - Седан 2020-2024",
                "CT5 - Седан 2020-2024",
                "XT4 - Кроссовер 2018-2024",
                "XT5 - Кроссовер 2017-2024",
                "XT6 - Кроссовер 2019-2024",
                "Escalade - Внедорожник 2015-2024"
        ));

        brands.put("Acura", Arrays.asList(
                "TLX - Седан 2020-2024",
                "RDX - Кроссовер 2018-2024",
                "MDX - Кроссовер 2021-2024",
                "Integra - Лифтбек 2022-2024"
        ));

        return brands;
    }

    public static List<String> getAllBrands() {
        List<String> brands = new ArrayList<>(getBrandsWithModels().keySet());
        java.util.Collections.sort(brands);
        return brands;
    }

    public static List<String> getModelsForBrand(String brand) {
        List<String> models = getBrandsWithModels().get(brand);
        if (models == null) return new ArrayList<>();
        return new ArrayList<>(models);
    }
}