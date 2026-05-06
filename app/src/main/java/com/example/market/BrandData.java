package com.example.market;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BrandData {

    public static Map<String, List<String>> getBrandsWithModels() {
        Map<String, List<String>> brands = new LinkedHashMap<>();

        // BMW
        brands.put("BMW", Arrays.asList(
                "1 серия III (F40) 2019-2024",
                "1 серия II (F20/F21) 2011-2019",
                "1 серия I (E81/E82/E87/E88) 2004-2013",
                "2 серия Active Tourer II (U06) 2021-н.в.",
                "2 серия Active Tourer (F45) 2014-2021",
                "2 серия Gran Coupe (F44) 2020-2024",
                "2 серия Coupe II (G42) 2021-2024",
                "2 серия Coupe (F22/F23) 2013-2021",
                "3 серия VII (G20/G21) 2018-2024",
                "3 серия VI (F30/F31/F34) 2011-2019",
                "3 серия V (E90/E91/E92/E93) 2005-2013",
                "3 серия IV (E46) 1998-2006",
                "4 серия II (G22/G23/G26) 2020-2024",
                "4 серия I (F32/F33/F36) 2013-2020",
                "5 серия VIII (G60/G61) 2023-2026",
                "5 серия VII (G30/G31) 2016-2023",
                "5 серия VI (F10/F11/F07) 2009-2017",
                "5 серия V (E60/E61) 2003-2010",
                "6 серия III (G32) 2017-2023",
                "6 серия II (F12/F13/F06) 2010-2018",
                "6 серия I (E63/E64) 2003-2010",
                "7 серия VI (G70) 2022-2026",
                "7 серия V (G11/G12) 2015-2022",
                "7 серия IV (F01/F02) 2008-2015",
                "7 серия III (E65/E66) 2001-2008",
                "8 серия II (G14/G15/G16) 2018-2024",
                "8 серия I (E31) 1989-1999",
                "X1 II (U11) 2022-н.в.",
                "X1 I (E84) 2009-2015",
                "X1 II (F48) 2015-2022",
                "X2 (U10) 2023-н.в.",
                "X2 I (F39) 2017-2023",
                "X3 IV (G45) 2024-н.в.",
                "X3 III (G01) 2017-2024",
                "X3 II (F25) 2010-2017",
                "X3 I (E83) 2003-2010",
                "X4 II (G02) 2018-2024",
                "X4 I (F26) 2014-2018",
                "X5 IV (G05) 2018-2023",
                "X5 III (F15) 2013-2018",
                "X5 II (E70) 2006-2013",
                "X5 I (E53) 1999-2006",
                "X6 III (G06) 2019-2023",
                "X6 II (F16) 2014-2019",
                "X6 I (E71/E72) 2007-2014",
                "X7 (G07) 2018-2022",
                "i3 (G28) 2022-н.в.",
                "i4 (G26 BEV) 2021-2024",
                "i5 (G60 BEV) 2023-н.в.",
                "i7 (G70 BEV) 2022-2026",
                "iX1 (U11 BEV) 2022-н.в.",
                "iX3 (G08) 2020-2024",
                "iX (I20) 2021-2025",
                "M2 II (G87) 2022-н.в.",
                "M2 I (F87) 2014-2021",
                "M3 IV (G80) 2020-2024",
                "M3 III (F80) 2014-2018",
                "M3 II (E90/E92/E93) 2007-2013",
                "M4 II (G82/G83) 2020-2024",
                "M4 I (F82/F83) 2014-2020",
                "M5 V (G90) 2024-н.в.",
                "M5 IV (F90) 2017-2023",
                "M5 III (F10) 2011-2016",
                "M8 (F91/F92/F93) 2019-2024",
                "Z4 III (G29) 2018-2024",
                "Z4 II (E89) 2009-2016",
                "Z4 I (E85/E86) 2002-2008",
                "XM (G09) 2022-н.в."
        ));

        // Mercedes-Benz
        brands.put("Mercedes-Benz", Arrays.asList(
                "A-Class IV (W177) 2018-2024",
                "A-Class III (W176) 2012-2018",
                "A-Class Sedan (V177) 2018-2024",
                "B-Class III (W247) 2019-2024",
                "B-Class II (W246) 2011-2019",
                "C-Class V (W206) 2021-2024",
                "C-Class IV (W205) 2014-2021",
                "C-Class III (W204) 2007-2014",
                "C-Class Estate V (S206) 2021-2024",
                "C-Class Estate IV (S205) 2014-2021",
                "E-Class VI (W214) 2023-2024",
                "E-Class V (W213) 2016-2023",
                "E-Class IV (W212) 2009-2016",
                "E-Class Estate VI (S214) 2023-2024",
                "E-Class Estate V (S213) 2016-2023",
                "S-Class VII (W223) 2020-2024",
                "S-Class VI (W222) 2013-2020",
                "S-Class V (W221) 2005-2013",
                "CLA II (C118) 2019-2024",
                "CLA I (C117) 2013-2019",
                "CLS III (C257) 2018-2023",
                "CLS II (C218) 2010-2018",
                "GLA II (H247) 2020-2024",
                "GLA I (X156) 2013-2020",
                "GLB (X247) 2019-2024",
                "GLC II (X254) 2022-2024",
                "GLC I (X253) 2015-2022",
                "GLC Coupe II (C254) 2023-2024",
                "GLC Coupe I (C253) 2016-2023",
                "GLE II (W167) 2018-2023",
                "GLE I (W166) 2011-2018",
                "GLE Coupe II (C167) 2020-2023",
                "GLE Coupe I (C292) 2015-2019",
                "GLS II (X167) 2019-2023",
                "GLS I (X166) 2013-2019",
                "G-Class III (W463) 2018-2024",
                "G-Class II (W463) 1990-2018",
                "EQS (V297) 2021-2024",
                "EQE (V295) 2022-2024",
                "EQA (H243) 2021-2024",
                "EQB (X243) 2021-2024",
                "AMG GT (C192) 2014-2024",
                "AMG GT 4-Door (X290) 2018-2024",
                "SL VII (R232) 2021-2024",
                "SL VI (R231) 2012-2020",
                "V-Class III (W447) 2014-2024",
                "V-Class II (W639) 2003-2014",
                "Viano (W639) 2003-2014",
                "Sprinter III (VS30) 2018-2024",
                "Sprinter II (NCV3) 2006-2018"
        ));

        // Audi
        brands.put("Audi", Arrays.asList(
                "A1 II (GB) 2018-2024",
                "A1 I (8X) 2010-2018",
                "A3 IV (8Y) 2020-2024",
                "A3 III (8V) 2012-2020",
                "A3 II (8P) 2003-2012",
                "A3 Sedan IV (8Y) 2020-2024",
                "A3 Sedan III (8V) 2013-2020",
                "A4 V (B9) 2015-2023",
                "A4 IV (B8) 2007-2015",
                "A4 Avant V (B9) 2015-2023",
                "A4 Avant IV (B8) 2007-2015",
                "A5 II (F5) 2016-2024",
                "A5 I (8T) 2007-2016",
                "A5 Sportback II (F5) 2016-2024",
                "A5 Sportback I (8T) 2009-2016",
                "A6 V (C8) 2018-2024",
                "A6 IV (C7) 2011-2018",
                "A6 Avant V (C8) 2018-2024",
                "A6 Avant IV (C7) 2011-2018",
                "A7 II (C8) 2018-2024",
                "A7 I (C7) 2010-2018",
                "A8 IV (D5) 2017-2024",
                "A8 III (D4) 2009-2017",
                "Q2 (GA) 2016-2024",
                "Q3 II (F3) 2018-2024",
                "Q3 I (8U) 2011-2018",
                "Q3 Sportback (F3) 2019-2024",
                "Q5 II (FY) 2017-2024",
                "Q5 I (8R) 2008-2017",
                "Q5 Sportback (FY) 2020-2024",
                "Q7 II (4M) 2015-2024",
                "Q7 I (4L) 2005-2015",
                "Q8 (4MN) 2018-2024",
                "e-tron (GE) 2019-2024",
                "e-tron Sportback (GE) 2019-2024",
                "e-tron GT (J1) 2021-2024",
                "Q4 e-tron (F3) 2021-2024",
                "Q4 e-tron Sportback (F3) 2021-2024",
                "Q6 e-tron (PPE) 2023-2024",
                "TT III (FV) 2014-2023",
                "TT II (8J) 2006-2014",
                "R8 II (4S) 2015-2024",
                "R8 I (42) 2006-2015",
                "RS3 IV (8Y) 2021-2024",
                "RS3 III (8V) 2015-2020",
                "RS4 Avant III (B9) 2017-2024",
                "RS5 II (F5) 2017-2024",
                "RS6 Avant IV (C8) 2019-2024",
                "RS6 Avant III (C7) 2013-2018",
                "RS7 II (C8) 2019-2024",
                "RS7 I (C7) 2013-2018",
                "RS Q8 (4MN) 2019-2024",
                "SQ5 II (FY) 2017-2024",
                "SQ7 II (4M) 2016-2024",
                "SQ8 (4MN) 2019-2024"
        ));

        // Toyota
        brands.put("Toyota", Arrays.asList(
                "Corolla XII (E210) 2018-2024",
                "Corolla XI (E170) 2013-2018",
                "Corolla X (E150) 2006-2013",
                "Corolla Cross (XG10) 2021-2024",
                "Camry XV70 (2017-2024)",
                "Camry XV50 (2011-2017)",
                "Camry XV40 (2006-2011)",
                "Yaris IV (XP210) 2020-2024",
                "Yaris III (XP130) 2011-2020",
                "Yaris Cross (XP210) 2020-2024",
                "Prius V (XW60) 2022-2024",
                "Prius IV (XW50) 2015-2022",
                "Prius III (XW30) 2009-2015",
                "RAV4 V (XA50) 2018-2024",
                "RAV4 IV (XA40) 2013-2018",
                "RAV4 III (XA30) 2005-2013",
                "Highlander IV (XU70) 2019-2024",
                "Highlander III (XU50) 2013-2019",
                "Land Cruiser 300 (J300) 2021-2024",
                "Land Cruiser 200 (J200) 2007-2021",
                "Land Cruiser 100 (J100) 1998-2007",
                "Land Cruiser Prado 250 (J250) 2023-2024",
                "Land Cruiser Prado 150 (J150) 2009-2023",
                "Land Cruiser Prado 120 (J120) 2002-2009",
                "Fortuner II (AN160) 2015-2024",
                "Fortuner I (AN50) 2004-2015",
                "Hilux VIII (AN120) 2015-2024",
                "Hilux VII (AN10) 2004-2015",
                "Tundra III (XK70) 2021-2024",
                "Tundra II (XK50) 2007-2021",
                "Tacoma IV (N400) 2023-2024",
                "Tacoma III (N300) 2015-2023",
                "C-HR (AX50) 2016-2024",
                "Supra V (A90) 2019-2024",
                "Supra IV (A80) 1993-2002",
                "GR86 (ZN8) 2021-2024",
                "GT86 (ZN6) 2012-2021",
                "GR Yaris (XP210) 2020-2024",
                "bZ4X (EA10) 2022-2024",
                "Sienna IV (XL40) 2020-2024",
                "Sienna III (XL30) 2010-2020",
                "Venza II (XU80) 2020-2024",
                "Venza I (AV10) 2008-2016",
                "Avalon V (XX50) 2018-2022",
                "Avalon IV (XX40) 2012-2018"
        ));

        // Kia
        brands.put("Kia", Arrays.asList(
                "Rio IV (YB) 2017-2024",
                "Rio III (UB) 2011-2017",
                "Cerato IV (BD) 2018-2024",
                "Cerato III (YD) 2013-2018",
                "K5 III (DL3) 2019-2024",
                "Optima IV (JF) 2015-2019",
                "Optima III (TF) 2010-2015",
                "Stinger (CK) 2017-2023",
                "K8 (GL3) 2021-2024",
                "K9 II (RJ) 2018-2024",
                "K9 I (KH) 2012-2018",
                "Sportage V (NQ5) 2021-2024",
                "Sportage IV (QL) 2016-2021",
                "Sportage III (SL) 2010-2016",
                "Sorento IV (MQ4) 2020-2024",
                "Sorento III (UM) 2014-2020",
                "Sorento II (XM) 2009-2014",
                "Telluride (ON) 2019-2024",
                "Mohave (HM) 2008-2024",
                "Seltos (SP2) 2019-2024",
                "Soul III (SK3) 2019-2024",
                "Soul II (PS) 2013-2019",
                "Niro II (SG2) 2021-2024",
                "Niro I (DE) 2016-2021",
                "EV6 (CV) 2021-2024",
                "EV9 (MV) 2023-2024",
                "Carnival IV (KA4) 2020-2024",
                "Carnival III (YP) 2014-2020",
                "Ceed III (CD) 2018-2024",
                "Ceed II (JD) 2012-2018",
                "ProCeed III (CD) 2018-2024",
                "XCeed (CD) 2019-2024",
                "Picanto III (JA) 2017-2024",
                "Picanto II (TA) 2011-2017"
        ));

        // Hyundai
        brands.put("Hyundai", Arrays.asList(
                "Solaris II (HC) 2017-2024",
                "Solaris I (RB) 2010-2017",
                "Accent V (HC) 2017-2024",
                "Elantra VII (CN7) 2020-2024",
                "Elantra VI (AD) 2015-2020",
                "Elantra V (MD) 2010-2015",
                "Sonata VIII (DN8) 2019-2024",
                "Sonata VII (LF) 2014-2019",
                "Grandeur VII (GN7) 2022-2024",
                "Grandeur VI (IG) 2016-2022",
                "Creta II (SU2) 2019-2024",
                "Creta I (GS) 2014-2019",
                "Tucson IV (NX4) 2020-2024",
                "Tucson III (TL) 2015-2020",
                "Tucson II (LM) 2009-2015",
                "Santa Fe V (MX5) 2023-2024",
                "Santa Fe IV (TM) 2018-2023",
                "Santa Fe III (DM) 2012-2018",
                "Palisade (LX2) 2018-2024",
                "i10 III (AC3) 2019-2024",
                "i20 III (BC3) 2020-2024",
                "i20 II (IB) 2014-2020",
                "i30 III (PD) 2016-2024",
                "i30 II (GD) 2011-2016",
                "Ioniq 5 (NE) 2021-2024",
                "Ioniq 6 (CE) 2022-2024",
                "Kona II (SX2) 2023-2024",
                "Kona I (OS) 2017-2023",
                "Bayon (BC3) 2021-2024",
                "Staria (US4) 2021-2024",
                "Porter II (HR) 2004-2024"
        ));

        // Lada
        brands.put("Lada (ВАЗ)", Arrays.asList(
                "Vesta I (NG) 2015-2024",
                "Vesta SW 2017-2024",
                "Vesta SW Cross 2017-2024",
                "Granta I (2190) 2011-2024",
                "Granta Cross 2019-2024",
                "Granta Liftback 2018-2024",
                "Kalina II (2192/2194) 2013-2018",
                "Kalina I (1117/1118/1119) 2004-2013",
                "Priora (2170/2171/2172) 2007-2018",
                "Largus I (R90) 2012-2024",
                "Largus Cross 2015-2024",
                "Niva Legend (2121) 1977-2024",
                "Niva Travel (2123) 2020-2024",
                "XRAY (GAB) 2015-2022",
                "XRAY Cross 2018-2022",
                "2110/2111/2112 1995-2010",
                "2108/2109/21099 (Самара) 1984-2013",
                "2105/2107 1980-2012",
                "2101/2102/2103/2104/2106 1970-2005",
                "Ока (1111) 1988-2008",
                "Нива (2121) 1977-2024"
        ));

        // Volkswagen
        brands.put("Volkswagen", Arrays.asList(
                "Polo VI (AW) 2017-2024",
                "Polo V (6R) 2009-2017",
                "Polo Sedan (CK) 2020-2024",
                "Golf VIII (CD) 2019-2024",
                "Golf VII (5G) 2012-2020",
                "Golf VI (5K) 2008-2012",
                "Passat B9 (2024-н.в.)",
                "Passat B8 (2014-2022)",
                "Passat B7 (2010-2015)",
                "Passat B6 (2005-2010)",
                "Jetta VII (BU) 2018-2024",
                "Jetta VI (NF) 2010-2018",
                "Tiguan II (AD) 2016-2024",
                "Tiguan I (5N) 2007-2016",
                "Touareg III (CR) 2018-2024",
                "Touareg II (7P) 2010-2018",
                "Teramont (CA) 2017-2024",
                "Taos (BZ) 2018-2024",
                "T-Roc (A11) 2017-2024",
                "T-Cross (C11) 2019-2024",
                "ID.3 (E11) 2019-2024",
                "ID.4 (E21) 2020-2024",
                "ID.5 (E31) 2021-2024",
                "ID.7 (VW413) 2023-2024",
                "Multivan T7 (2021-2024)",
                "Multivan T6 (2015-2021)",
                "Amarok II (2G) 2022-2024",
                "Amarok I (2H) 2010-2020",
                "Caddy V (SB) 2020-2024",
                "Caddy IV (SA) 2015-2020"
        ));

        // Ford
        brands.put("Ford", Arrays.asList(
                "Focus IV (C519) 2018-2024",
                "Focus III (C346) 2010-2018",
                "Focus II (C307) 2004-2010",
                "Fiesta VII (B479) 2017-2023",
                "Fiesta VI (B299) 2008-2017",
                "Mondeo V (CD391) 2014-2022",
                "Mondeo IV (CD345) 2007-2014",
                "Mustang VII (S650) 2024-н.в.",
                "Mustang VI (S550) 2015-2023",
                "Mustang Mach-E (CX727) 2020-2024",
                "Explorer VI (U625) 2019-2024",
                "Explorer V (U502) 2011-2019",
                "Kuga III (CX482) 2019-2024",
                "Kuga II (C520) 2013-2019",
                "Edge II (CD539) 2015-2024",
                "EcoSport II (B515) 2017-2023",
                "Puma (B539) 2019-2024",
                "Ranger IV (P703) 2022-2024",
                "Ranger III (P375) 2011-2022",
                "F-150 XIV (P702) 2021-2024",
                "F-150 XIII (P552) 2015-2020",
                "Transit Custom II (2012-2024)",
                "Transit V (2013-2024)"
        ));

        // Добавим остальные марки покороче
        brands.put("Nissan", Arrays.asList(
                "Almera III (N18) 2019-2024",
                "Sentra VIII (B18) 2019-2024",
                "Altima VI (L34) 2018-2024",
                "Qashqai III (J12) 2021-2024",
                "Qashqai II (J11) 2013-2021",
                "X-Trail IV (T33) 2022-2024",
                "X-Trail III (T32) 2013-2022",
                "Murano III (Z53) 2015-2024",
                "Pathfinder V (R53) 2021-2024",
                "Juke II (F16) 2019-2024",
                "Juke I (F15) 2010-2019",
                "Ariya (FE0) 2022-2024",
                "Leaf II (ZE1) 2017-2024",
                "GT-R (R35) 2007-2024",
                "Navara III (D23) 2014-2024"
        ));

        brands.put("Renault", Arrays.asList(
                "Logan III (2020-2024)",
                "Logan II (2012-2020)",
                "Sandero III (2020-2024)",
                "Sandero II (2012-2020)",
                "Duster II (2017-2024)",
                "Duster I (2009-2017)",
                "Arkana (2019-2024)",
                "Kaptur (2016-2024)",
                "Megane IV (2016-2024)",
                "Koleos II (2016-2024)"
        ));

        brands.put("Mazda", Arrays.asList(
                "Mazda3 IV (BP) 2019-2024",
                "Mazda3 III (BM) 2013-2019",
                "Mazda6 III (GJ) 2012-2024",
                "CX-5 II (KF) 2017-2024",
                "CX-5 I (KE) 2012-2017",
                "CX-9 II (TC) 2016-2024",
                "CX-30 (DM) 2019-2024",
                "CX-50 (VA) 2022-2024",
                "MX-5 IV (ND) 2015-2024"
        ));

        brands.put("Lexus", Arrays.asList(
                "ES VII (XZ10) 2018-2024",
                "LS V (XF50) 2017-2024",
                "NX II (AZ20) 2021-2024",
                "NX I (AZ10) 2014-2021",
                "RX V (AL20) 2022-2024",
                "RX IV (AL10) 2015-2022",
                "LX IV (J310) 2021-2024",
                "LX III (J200) 2007-2021",
                "GX II (J250) 2023-2024",
                "UX (ZA10) 2018-2024"
        ));

        brands.put("Honda", Arrays.asList(
                "Civic XI (FE) 2021-2024",
                "Civic X (FC) 2015-2021",
                "Accord X (CV) 2017-2022",
                "CR-V VI (RS) 2022-2024",
                "CR-V V (RW) 2017-2022",
                "HR-V III (RV) 2021-2024",
                "Pilot IV (YG) 2023-2024",
                "Pilot III (YF) 2016-2022"
        ));

        brands.put("Subaru", Arrays.asList(
                "Outback VI (BT) 2019-2024",
                "Outback V (BS) 2014-2019",
                "Forester V (SK) 2018-2024",
                "Forester IV (SJ) 2012-2018",
                "XV II (GT) 2017-2024",
                "Levorg II (VN) 2020-2024",
                "WRX II (VB) 2021-2024"
        ));

        brands.put("Mitsubishi", Arrays.asList(
                "Outlander IV (GF) 2021-2024",
                "Outlander III (GF) 2012-2021",
                "ASX II (GA) 2010-2024",
                "Pajero Sport III (KR) 2015-2024",
                "L200 VI (KJ) 2015-2024",
                "Eclipse Cross (GK) 2017-2024"
        ));

        brands.put("Porsche", Arrays.asList(
                "911 VIII (992) 2019-2024",
                "911 VII (991) 2011-2019",
                "Taycan (J1) 2019-2024",
                "Panamera III (G3) 2023-2024",
                "Panamera II (G2) 2016-2023",
                "Cayenne III (E3) 2017-2024",
                "Cayenne II (E2) 2010-2017",
                "Macan (95B) 2014-2024"
        ));

        brands.put("Tesla", Arrays.asList(
                "Model 3 (2017-2024)",
                "Model Y (2020-2024)",
                "Model S (2012-2024)",
                "Model X (2015-2024)",
                "Cybertruck (2023-2024)"
        ));

        brands.put("Volvo", Arrays.asList(
                "S60 III (2018-2024)",
                "S90 II (2016-2024)",
                "V60 II (2018-2024)",
                "V90 II (2016-2024)",
                "XC40 (2017-2024)",
                "XC60 II (2017-2024)",
                "XC90 II (2015-2024)"
        ));

        brands.put("Land Rover", Arrays.asList(
                "Range Rover V (L460) 2022-2024",
                "Range Rover IV (L405) 2013-2022",
                "Range Rover Sport III (L461) 2022-2024",
                "Range Rover Sport II (L494) 2013-2022",
                "Range Rover Velar (L560) 2017-2024",
                "Range Rover Evoque II (L551) 2018-2024",
                "Defender II (L663) 2020-2024",
                "Discovery V (L462) 2017-2024",
                "Discovery Sport (L550) 2014-2024"
        ));

        brands.put("Chevrolet", Arrays.asList(
                "Camaro VI (2016-2024)",
                "Corvette C8 (2020-2024)",
                "Tahoe V (2021-2024)",
                "Suburban XII (2021-2024)",
                "Traverse II (2018-2024)",
                "Equinox III (2018-2024)",
                "Trax II (2024-н.в.)",
                "Malibu IX (2016-2024)",
                "Silverado IV (2019-2024)"
        ));

        brands.put("Jeep", Arrays.asList(
                "Grand Cherokee V (WL) 2021-2024",
                "Grand Cherokee IV (WK2) 2010-2021",
                "Wrangler IV (JL) 2017-2024",
                "Compass II (MP) 2016-2024",
                "Renegade (BU) 2014-2024",
                "Cherokee V (KL) 2013-2024"
        ));

        brands.put("Skoda", Arrays.asList(
                "Octavia IV (NX) 2020-2024",
                "Octavia III (5E) 2012-2020",
                "Superb IV (2023-2024)",
                "Superb III (3V) 2015-2023",
                "Kodiaq (NS) 2016-2024",
                "Karoq (NU) 2017-2024",
                "Kamiq (NW) 2019-2024",
                "Fabia IV (PJ) 2021-2024"
        ));

        brands.put("Peugeot", Arrays.asList(
                "308 III (P5) 2021-2024",
                "408 II (P5) 2022-2024",
                "2008 II (P2) 2019-2024",
                "3008 II (P8) 2016-2024",
                "5008 II (P8) 2017-2024"
        ));

        brands.put("Citroen", Arrays.asList(
                "C3 IV (2024-н.в.)",
                "C3 III (2016-2024)",
                "C4 III (2020-2024)",
                "C5 Aircross (2018-2024)",
                "C5 X (2021-2024)"
        ));

        brands.put("Opel", Arrays.asList(
                "Corsa VI (2019-2024)",
                "Astra L (2021-2024)",
                "Astra K (2015-2021)",
                "Grandland X (2017-2024)",
                "Mokka II (2020-2024)",
                "Zafira Life (2019-2024)"
        ));

        brands.put("Chery", Arrays.asList(
                "Tiggo 4 (T1X) 2017-2024",
                "Tiggo 7 (T1X) 2016-2024",
                "Tiggo 8 (T1X) 2018-2024",
                "Arrizo 5 (2016-2024)",
                "Arrizo 8 (2022-2024)"
        ));

        brands.put("Haval", Arrays.asList(
                "Jolion (B06) 2020-2024",
                "F7 (2018-2024)",
                "Dargo (B06) 2021-2024",
                "H6 III (2020-2024)",
                "H9 (2014-2024)"
        ));

        brands.put("Geely", Arrays.asList(
                "Atlas (NL-3) 2016-2024",
                "Coolray (SX11) 2018-2024",
                "Tugella (FY11) 2019-2024",
                "Monjaro (KX11) 2021-2024",
                "Atlas Pro (2020-2024)"
        ));

        brands.put("Infiniti", Arrays.asList(
                "Q50 (V37) 2013-2024",
                "Q60 (CV37) 2016-2022",
                "QX50 II (P71A) 2017-2024",
                "QX60 II (L51) 2021-2024",
                "QX80 II (Z63) 2024-н.в.",
                "QX80 I (Z62) 2010-2024"
        ));

        brands.put("Genesis", Arrays.asList(
                "G70 (IK) 2017-2024",
                "G80 II (RG3) 2020-2024",
                "G90 II (RS4) 2021-2024",
                "GV60 (JW) 2021-2024",
                "GV70 (JK1) 2020-2024",
                "GV80 (JX1) 2020-2024"
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