package io.github.irfnhanif.rifasims;

import io.github.irfnhanif.rifasims.dto.RecommendedBarcodeScanResponse;
import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import io.github.irfnhanif.rifasims.entity.StockAuditLog;
import io.github.irfnhanif.rifasims.util.ScanItemRecommender;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Izinkan @BeforeAll & @AfterAll non-statis
class ScanItemRecommenderTest {
    private ScanItemRecommender recommender;
    private Map<TestOutcome, Integer> resultsSummary;
    // Enum untuk hasil perbandingan yang lebih deskriptif
    enum TestOutcome {
        ALGORITHM_WINS,   // Algoritma Sukses, Standar Gagal (b)
        STANDARD_WINS,    // Algoritma Gagal, Standar Sukses (c)
        BOTH_WIN,         // Keduanya Sukses (a)
        BOTH_FAIL         // Keduanya Gagal (d)
    }


    @BeforeAll
    void globalSetUp() {
        recommender = new ScanItemRecommender();
        resultsSummary = new EnumMap<>(TestOutcome.class);
        // Inisialisasi map
        for (TestOutcome outcome : TestOutcome.values()) {
            resultsSummary.put(outcome, 0);
        }
        System.out.println("Memulai Pengujian Komparatif...");
    }

    @AfterAll
    void showSummary() {
        System.out.println("\n--- REKAPITULASI HASIL PENGUJIAN ---");
        int b = resultsSummary.getOrDefault(TestOutcome.ALGORITHM_WINS, 0);
        int c = resultsSummary.getOrDefault(TestOutcome.STANDARD_WINS, 0);
        System.out.printf("A (Keduanya Sukses): %d\n", resultsSummary.getOrDefault(TestOutcome.BOTH_WIN, 0));
        System.out.printf("B (Algoritma Unggul): %d\n", b);
        System.out.printf("C (Standar Unggul) : %d\n", c);
        System.out.printf("D (Keduanya Gagal) : %d\n", resultsSummary.getOrDefault(TestOutcome.BOTH_FAIL, 0));
        System.out.println("-------------------------------------");
        System.out.printf("Total Kasus Diskordan (b + c): %d\n", (b + c));
        if (b + c > 25) {
            System.out.println("Jumlah kasus diskordan CUKUP untuk McNemar's Test dengan aproksimasi Chi-Kuadrat.");
        } else {
            System.out.println("PERINGATAN: Jumlah kasus diskordan RENDAH (< 25). Pertimbangkan menambah skenario atau menggunakan Exact Test.");
        }
    }

    /**
     * Metode inti yang diekstrak untuk menjalankan satu skenario uji.
     *
     * @param scenarioName Nama skenario untuk logging.
     * @param matchingItems Daftar item yang cocok dengan barcode.
     * @param scanHistory Riwayat pindaian.
     * @param expectedTopItem Item yang seharusnya menjadi rekomendasi teratas.
     */

    private void runAndRecordTest(String scenarioName, List<ItemStock> matchingItems, List<StockAuditLog> scanHistory, ItemStock expectedTopItem) {
        System.out.printf("\n--- Menjalankan Skenario: %s ---\n", scenarioName);
        System.out.printf("Hasil yang diharapkan: %s \n", expectedTopItem.getItem().getName());
        // 1. Uji Algoritma Rekomendasi
        List<RecommendedBarcodeScanResponse> recommendedResult = recommender.recommendItem(matchingItems, scanHistory);

//        System.out.println("Recommendations:");
//        for (RecommendedBarcodeScanResponse item : recommendedResult) {
//            System.out.println(" - " + item.getItemName() + " (Score: " + item.getRecommendationScore() + ")");
//        }

        boolean algorithmSuccess = !recommendedResult.isEmpty() && expectedTopItem.getItem().getName().equals(recommendedResult.get(0).getItemName());

        System.out.println("Hasil Algoritma: " + (recommendedResult.isEmpty() ? "Kosong" : recommendedResult.get(0).getItemName()) + " -> " + (algorithmSuccess ? "SUKSES" : "GAGAL"));

        // 2. Uji Metode Standar (misal: urut abjad)
        List<ItemStock> standardSortedList = new ArrayList<>(matchingItems);
        standardSortedList.sort(Comparator.comparing(is -> is.getItem().getName()));
        boolean standardSuccess = !standardSortedList.isEmpty() && expectedTopItem.getItem().getName().equals(standardSortedList.get(0).getItem().getName());
        System.out.println("Hasil Standar (Abjad): " + (standardSortedList.isEmpty() ? "Kosong" : standardSortedList.get(0).getItem().getName()) + " -> " + (standardSuccess ? "SUKSES" : "GAGAL"));

        // 3. Tentukan hasil dan catat
        TestOutcome outcome;
        if (algorithmSuccess && !standardSuccess) {
            outcome = TestOutcome.ALGORITHM_WINS;
        } else if (!algorithmSuccess && standardSuccess) {
            outcome = TestOutcome.STANDARD_WINS;
        } else if (algorithmSuccess && standardSuccess) {
            outcome = TestOutcome.BOTH_WIN;
        } else {
            outcome = TestOutcome.BOTH_FAIL;
        }

        resultsSummary.merge(outcome, 1, Integer::sum);
    }

    // --- MULAI DEFINISIKAN SKENARIO-SKENARIO ANDA DI SINI ---

    @Test
    void scenario1_MostRecentAndFrequentIsTheSame() {
        Item itemA = new Item(UUID.randomUUID(),"A1", "BARCODE-123");
        Item itemB = new Item(UUID.randomUUID(),"A2", "BARCODE-456");
        Item itemC = new Item(UUID.randomUUID(),"A3", "BARCODE-789");

        ItemStock stockA = new ItemStock(itemA, 10);
        ItemStock stockB = new ItemStock(itemB, 10);
        ItemStock stockC = new ItemStock(itemC, 10);

        List<ItemStock> matchingItems = List.of(stockA, stockB, stockC);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemA.getId(), itemA.getBarcode(), itemA.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemA.getId(), itemA.getBarcode(), itemA.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemB.getId(),itemB.getBarcode(), itemB.getName(), LocalDateTime.now().minusDays(10))
        );

        runAndRecordTest("(1) Sering & Baru Sama", matchingItems, history, stockA);
    }

    @Test
    void scenario2_Conflict_FrequencyWins_Over_Recency() {
        // TUJUAN: Menguji bobot 0.6 Frekuensi vs 0.4 Kebaruan. Frekuensi harus lebih kuat.

        Item itemB1 = new Item(UUID.randomUUID(), "B1", "BC-B1");
        Item itemB2 = new Item(UUID.randomUUID(), "B2", "BC-B2");
        Item itemB3 = new Item(UUID.randomUUID(), "B3", "BC-B3");

        ItemStock stockB1 = new ItemStock(itemB1, 100);
        ItemStock stockB2 = new ItemStock(itemB2, 100);
        ItemStock stockB3 = new ItemStock(itemB3, 100);
        List<ItemStock> matchingItems = List.of(stockB1, stockB2, stockB3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemB2.getId(), itemB2.getBarcode(), itemB2.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemB2.getId(), itemB2.getBarcode(), itemB2.getName(), LocalDateTime.now().minusDays(21)),
                new StockAuditLog(itemB2.getId(), itemB2.getBarcode(), itemB2.getName(), LocalDateTime.now().minusDays(22)),
                new StockAuditLog(itemB2.getId(), itemB2.getBarcode(), itemB2.getName(), LocalDateTime.now().minusDays(23)),
                new StockAuditLog(itemB2.getId(), itemB2.getBarcode(), itemB2.getName(), LocalDateTime.now().minusDays(24)),
                new StockAuditLog(itemB2.getId(), itemB2.getBarcode(), itemB2.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemB3.getId(), itemB3.getBarcode(), itemB3.getName(), LocalDateTime.now().minusDays(1))
        );

        runAndRecordTest("(2) Konflik: Frekuensi Unggul", matchingItems, history, stockB2);
    }

    @Test
    void scenario3_Conflict_ExtremeRecencyWins() {
        // TUJUAN: Menguji titik impas. Jika item sangat-sangat baru, ia bisa mengalahkan frekuensi sedang.

        Item itemC1 = new Item(UUID.randomUUID(), "C1", "BC-C1");
        Item itemC2 = new Item(UUID.randomUUID(), "C2", "BC-C2");
        Item itemC3 = new Item(UUID.randomUUID(), "C3", "BC-C3");

        ItemStock stockC1 = new ItemStock(itemC1, 100);
        ItemStock stockC2 = new ItemStock(itemC2, 100);
        ItemStock stockC3 = new ItemStock(itemC3, 100);
        List<ItemStock> matchingItems = List.of(stockC1, stockC2, stockC3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemC2.getId(), itemC2.getBarcode(), itemC2.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemC2.getId(), itemC2.getBarcode(), itemC2.getName(), LocalDateTime.now().minusDays(26)),
                new StockAuditLog(itemC2.getId(), itemC2.getBarcode(), itemC2.getName(), LocalDateTime.now().minusDays(27)),
                new StockAuditLog(itemC3.getId(), itemC3.getBarcode(), itemC3.getName(), LocalDateTime.now().minusHours(1))
        );

        runAndRecordTest("(3) Konflik: Kebaruan Ekstrem Unggul", matchingItems, history, stockC3);
    }

    @Test
    void scenario4_NoHistory_ColdStart() {
        // TUJUAN: Menguji kasus cold start total dimana tidak ada riwayat sama sekali.
        Item itemD1 = new Item(UUID.randomUUID(), "D1", "BC-D1");
        Item itemD2 = new Item(UUID.randomUUID(), "D2", "BC-D2");
        Item itemD3 = new Item(UUID.randomUUID(), "D3", "BC-D3");

        ItemStock stockD1 = new ItemStock(itemD1, 100);
        ItemStock stockD2 = new ItemStock(itemD2, 100);
        ItemStock stockD3 = new ItemStock(itemD3, 100);
        List<ItemStock> matchingItems = List.of(stockD1, stockD2, stockD3);

        List<StockAuditLog> history = Collections.emptyList();

        runAndRecordTest("(4) Tanpa Riwayat (Cold Start)", matchingItems, history, stockD2);
    }

    @Test
    void scenario5_CoincidentalWin_BothWin() {
        // TUJUAN: Membuat kasus di mana metode standar kebetulan benar.
        Item itemE1 = new Item(UUID.randomUUID(), "E1", "BC-E1");
        Item itemE2 = new Item(UUID.randomUUID(), "E2", "BC-E2");
        Item itemE3 = new Item(UUID.randomUUID(), "E3", "BC-E3");

        ItemStock stockE1 = new ItemStock(itemE1, 100);
        ItemStock stockE2 = new ItemStock(itemE2, 100);
        ItemStock stockE3 = new ItemStock(itemE3, 100);
        List<ItemStock> matchingItems = List.of(stockE1, stockE2, stockE3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemE1.getId(), itemE1.getBarcode(), itemE1.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemE1.getId(), itemE1.getBarcode(), itemE1.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemE2.getId(), itemE2.getBarcode(), itemE2.getName(), LocalDateTime.now().minusDays(20))
        );

        runAndRecordTest("(5) Jawaban Benar Secara Kebetulan", matchingItems, history, stockE1);
    }

    @Test
    void scenario6_OnlyOneItemHasHistory_PartialColdStart() {
        // TUJUAN: Menguji kasus cold start parsial.
        Item itemF1 = new Item(UUID.randomUUID(), "F1", "BC-F1");
        Item itemF2 = new Item(UUID.randomUUID(), "F2", "BC-F2");
        Item itemF3 = new Item(UUID.randomUUID(), "F3", "BC-F3");

        ItemStock stockF1 = new ItemStock(itemF1, 100);
        ItemStock stockF2 = new ItemStock(itemF2, 100);
        ItemStock stockF3 = new ItemStock(itemF3, 100);
        List<ItemStock> matchingItems = List.of(stockF1, stockF2, stockF3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemF2.getId(), itemF2.getBarcode(), itemF2.getName(), LocalDateTime.now().minusDays(29))
        );

        runAndRecordTest("(6) Hanya Satu Item Punya Riwayat", matchingItems, history, stockF2);
    }

    @Test
    void scenario7_SameFrequency_RecencyDecides() {
        // TUJUAN: Mengisolasi dan menguji komponen kebaruan.

        Item itemG1 = new Item(UUID.randomUUID(), "G1", "BC-G1");
        Item itemG2 = new Item(UUID.randomUUID(), "G2", "BC-G2");
        Item itemG3 = new Item(UUID.randomUUID(), "G3", "BC-G3");

        ItemStock stockG1 = new ItemStock(itemG1, 100);
        ItemStock stockG2 = new ItemStock(itemG2, 100);
        ItemStock stockG3 = new ItemStock(itemG3, 100);
        List<ItemStock> matchingItems = List.of(stockG1, stockG2, stockG3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemG1.getId(), itemG1.getBarcode(), itemG1.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemG1.getId(), itemG1.getBarcode(), itemG1.getName(), LocalDateTime.now().minusDays(21)),
                new StockAuditLog(itemG1.getId(), itemG1.getBarcode(), itemG1.getName(), LocalDateTime.now().minusDays(22)),
                new StockAuditLog(itemG2.getId(), itemG2.getBarcode(), itemG2.getName(), LocalDateTime.now().minusDays(10)),
                new StockAuditLog(itemG2.getId(), itemG2.getBarcode(), itemG2.getName(), LocalDateTime.now().minusDays(11)),
                new StockAuditLog(itemG2.getId(), itemG2.getBarcode(), itemG2.getName(), LocalDateTime.now().minusDays(12)),
                new StockAuditLog(itemG3.getId(), itemG3.getBarcode(), itemG3.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemG3.getId(), itemG3.getBarcode(), itemG3.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemG3.getId(), itemG3.getBarcode(), itemG3.getName(), LocalDateTime.now().minusDays(4))
        );

        runAndRecordTest("(7) Frekuensi Sama, Kebaruan Menentukan", matchingItems, history, stockG3);
    }

    @Test
    void scenario8_SameRecency_FrequencyDecides() {
        // TUJUAN: Mengisolasi dan menguji komponen frekuensi.

        Item itemH1 = new Item(UUID.randomUUID(), "H1", "BC-H1");
        Item itemH2 = new Item(UUID.randomUUID(), "H2", "BC-H2");
        Item itemH3 = new Item(UUID.randomUUID(), "H3", "BC-H3");

        ItemStock stockH1 = new ItemStock(itemH1, 100);
        ItemStock stockH2 = new ItemStock(itemH2, 100);
        ItemStock stockH3 = new ItemStock(itemH3, 100);
        List<ItemStock> matchingItems = List.of(stockH1, stockH2, stockH3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemH1.getId(), itemH1.getBarcode(), itemH1.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemH2.getId(), itemH2.getBarcode(), itemH2.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemH2.getId(), itemH2.getBarcode(), itemH2.getName(), LocalDateTime.now().minusDays(6)),
                new StockAuditLog(itemH2.getId(), itemH2.getBarcode(), itemH2.getName(), LocalDateTime.now().minusDays(7)),
                new StockAuditLog(itemH3.getId(), itemH3.getBarcode(), itemH3.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemH3.getId(), itemH3.getBarcode(), itemH3.getName(), LocalDateTime.now().minusDays(6))
        );

        runAndRecordTest("(8) Kebaruan Mirip, Frekuensi Menentukan", matchingItems, history, stockH2);
    }

    @Test
    void scenario9_StandardWinsByChance() {
        // TUJUAN: Membuat kasus di mana algoritma membuat kesalahan kecil dan standar kebetulan benar.

        Item itemI1 = new Item(UUID.randomUUID(), "I1", "BC-I1");
        Item itemI2 = new Item(UUID.randomUUID(), "I2", "BC-I2");
        Item itemI3 = new Item(UUID.randomUUID(), "I3", "BC-I3");

        ItemStock stockI1 = new ItemStock(itemI1, 100);
        ItemStock stockI2 = new ItemStock(itemI2, 100);
        ItemStock stockI3 = new ItemStock(itemI3, 100);
        List<ItemStock> matchingItems = List.of(stockI1, stockI2, stockI3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemI1.getId(), itemI1.getBarcode(), itemI1.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemI1.getId(), itemI1.getBarcode(), itemI1.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemI2.getId(), itemI2.getBarcode(), itemI2.getName(), LocalDateTime.now().minusDays(8)),
                new StockAuditLog(itemI2.getId(), itemI2.getBarcode(), itemI2.getName(), LocalDateTime.now().minusDays(9)),
                new StockAuditLog(itemI2.getId(), itemI2.getBarcode(), itemI2.getName(), LocalDateTime.now().minusDays(10))
        );

        runAndRecordTest("(9) Standar Unggul Secara Kebetulan", matchingItems, history, stockI1);
    }

    @Test
    void scenario10_ManyItems_WithNoise() {
        // TUJUAN: Menguji dengan lebih banyak pilihan dan data 'noise' yang tidak relevan.

        Item itemJ1 = new Item(UUID.randomUUID(), "J1", "BC-J");
        Item itemJ2 = new Item(UUID.randomUUID(), "J2", "BC-J");
        Item itemJ3 = new Item(UUID.randomUUID(), "J3", "BC-J");
        Item itemJ4 = new Item(UUID.randomUUID(), "J4", "BC-J");
        Item itemJ5 = new Item(UUID.randomUUID(), "J5", "BC-J");
        Item itemK1_Irrelevant = new Item(UUID.randomUUID(), "K1", "BC-K");

        ItemStock stockJ1 = new ItemStock(itemJ1, 100);
        ItemStock stockJ2 = new ItemStock(itemJ2, 100);
        ItemStock stockJ3 = new ItemStock(itemJ3, 100);
        ItemStock stockJ4 = new ItemStock(itemJ4, 100);
        ItemStock stockJ5 = new ItemStock(itemJ5, 100);
        List<ItemStock> matchingItems = List.of(stockJ1, stockJ2, stockJ3, stockJ4, stockJ5);

        List<StockAuditLog> history = new ArrayList<>(List.of(
                new StockAuditLog(itemJ1.getId(), itemJ1.getBarcode(), itemJ1.getName(), LocalDateTime.now().minusDays(10)),
                new StockAuditLog(itemJ2.getId(), itemJ2.getBarcode(), itemJ2.getName(), LocalDateTime.now().minusDays(15)),
                new StockAuditLog(itemJ3.getId(), itemJ3.getBarcode(), itemJ3.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemJ3.getId(), itemJ3.getBarcode(), itemJ3.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemJ3.getId(), itemJ3.getBarcode(), itemJ3.getName(), LocalDateTime.now().minusDays(4)),
                new StockAuditLog(itemJ4.getId(), itemJ4.getBarcode(), itemJ4.getName(), LocalDateTime.now().minusDays(1))
        ));

        history.add(new StockAuditLog(itemK1_Irrelevant.getId(), itemK1_Irrelevant.getBarcode(), itemK1_Irrelevant.getName(), LocalDateTime.now()));

        runAndRecordTest("(10) Banyak Item Dengan Noise", matchingItems, history, stockJ3);
    }

    @Test
    void scenario11_AllHistoryIsOld() {
        // TUJUAN: Menguji jika semua riwayat sudah lewat dari 30 hari (recency score = 0), keputusan murni dari frekuensi.

        Item itemK1 = new Item(UUID.randomUUID(), "K1", "BC-K1");
        Item itemK2 = new Item(UUID.randomUUID(), "K2", "BC-K2");
        Item itemK3 = new Item(UUID.randomUUID(), "K3", "BC-K3");

        ItemStock stockK1 = new ItemStock(itemK1, 100);
        ItemStock stockK2 = new ItemStock(itemK2, 100);
        ItemStock stockK3 = new ItemStock(itemK3, 100);
        List<ItemStock> matchingItems = List.of(stockK1, stockK2, stockK3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemK1.getId(), itemK1.getBarcode(), itemK1.getName(), LocalDateTime.now().minusDays(35)),
                new StockAuditLog(itemK2.getId(), itemK2.getBarcode(), itemK2.getName(), LocalDateTime.now().minusDays(40)),
                new StockAuditLog(itemK2.getId(), itemK2.getBarcode(), itemK2.getName(), LocalDateTime.now().minusDays(41)),
                new StockAuditLog(itemK3.getId(), itemK3.getBarcode(), itemK3.getName(), LocalDateTime.now().minusDays(50))
        );

        runAndRecordTest("(11) Semua Riwayat Lama", matchingItems, history, stockK2);
    }

    @Test
    void scenario12_ManyItems_WithSparseHistory() {
        // TUJUAN: Menguji dengan banyak pilihan barang tapi data riwayat sangat minim (sparse).

        Item itemL1 = new Item(UUID.randomUUID(), "L1", "BC-L1");
        Item itemL2 = new Item(UUID.randomUUID(), "L2", "BC-L2");
        Item itemL3 = new Item(UUID.randomUUID(), "L3", "BC-L3");
        Item itemL4 = new Item(UUID.randomUUID(), "L4", "BC-L4");
        Item itemL5 = new Item(UUID.randomUUID(), "L5", "BC-L5");

        ItemStock stockL1 = new ItemStock(itemL1, 100);
        ItemStock stockL2 = new ItemStock(itemL2, 100);
        ItemStock stockL3 = new ItemStock(itemL3, 100);
        ItemStock stockL4 = new ItemStock(itemL4, 100);
        ItemStock stockL5 = new ItemStock(itemL5, 100);
        List<ItemStock> matchingItems = List.of(stockL1, stockL2, stockL3, stockL4, stockL5);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemL2.getId(), itemL2.getBarcode(), itemL2.getName(), LocalDateTime.now().minusDays(15)),
                new StockAuditLog(itemL4.getId(), itemL4.getBarcode(), itemL4.getName(), LocalDateTime.now().minusDays(5))
        );

        runAndRecordTest("(12) Banyak Item, Riwayat Jarang", matchingItems, history, stockL4);
    }

    @Test
    void scenario13_TieInFinalScore_StandardWins() {
        // TUJUAN: Menciptakan kasus skor seri, di mana standar kebetulan menang.

        Item itemM1 = new Item(UUID.randomUUID(), "M1", "BC-M1");
        Item itemM2 = new Item(UUID.randomUUID(), "M2", "BC-M2");

        ItemStock stockM1 = new ItemStock(itemM1, 100);
        ItemStock stockM2 = new ItemStock(itemM2, 100);

        List<ItemStock> matchingItems = List.of(stockM2, stockM1);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemM1.getId(), itemM1.getBarcode(), itemM1.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemM2.getId(), itemM2.getBarcode(), itemM2.getName(), LocalDateTime.now().minusDays(10)),
                new StockAuditLog(itemM2.getId(), itemM2.getBarcode(), itemM2.getName(), LocalDateTime.now().minusDays(11))
        );

        runAndRecordTest("(13) Standar Unggul Karena Algoritma Salah", matchingItems, history, stockM1);
    }

    @Test
    void scenario14_AllItemsHaveHistory_DenseData() {
        // TUJUAN: Menguji di mana semua alternatif kompetitif dan memiliki riwayat.

        Item itemN1 = new Item(UUID.randomUUID(), "N1", "BC-N1");
        Item itemN2 = new Item(UUID.randomUUID(), "N2", "BC-N2");
        Item itemN3 = new Item(UUID.randomUUID(), "N3", "BC-N3");

        ItemStock stockN1 = new ItemStock(itemN1, 100);
        ItemStock stockN2 = new ItemStock(itemN2, 100);
        ItemStock stockN3 = new ItemStock(itemN3, 100);
        List<ItemStock> matchingItems = List.of(stockN1, stockN2, stockN3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemN1.getId(), itemN1.getBarcode(), itemN1.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemN1.getId(), itemN1.getBarcode(), itemN1.getName(), LocalDateTime.now().minusDays(4)),
                new StockAuditLog(itemN2.getId(), itemN2.getBarcode(), itemN2.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemN2.getId(), itemN2.getBarcode(), itemN2.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemN2.getId(), itemN2.getBarcode(), itemN2.getName(), LocalDateTime.now().minusDays(6)),
                new StockAuditLog(itemN3.getId(), itemN3.getBarcode(), itemN3.getName(), LocalDateTime.now().minusDays(15))
        );

        runAndRecordTest("(14) Semua Item Punya Riwayat", matchingItems, history, stockN2);
    }

    @Test
    void scenario15_AnotherCoincidentalWin_BothWin() {
        // TUJUAN: Menambah kasus BOTH_WIN

        Item itemO1 = new Item(UUID.randomUUID(), "O1", "BC-O1");
        Item itemO2 = new Item(UUID.randomUUID(), "O2", "BC-O2");
        Item itemO3 = new Item(UUID.randomUUID(), "O3", "BC-O3");

        ItemStock stockO1 = new ItemStock(itemO1, 100);
        ItemStock stockO2 = new ItemStock(itemO2, 100);
        ItemStock stockO3 = new ItemStock(itemO3, 100);
        List<ItemStock> matchingItems = List.of(stockO1, stockO2, stockO3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemO1.getId(), itemO1.getBarcode(), itemO1.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemO1.getId(), itemO1.getBarcode(), itemO1.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemO1.getId(), itemO1.getBarcode(), itemO1.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemO2.getId(), itemO2.getBarcode(), itemO2.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemO3.getId(), itemO3.getBarcode(), itemO3.getName(), LocalDateTime.now().minusDays(25))
        );

        runAndRecordTest("(15) Kasus BOTH_WIN Lainnya", matchingItems, history, stockO1);
    }

    @Test
    void scenario16_TwoStrongContenders() {
        // TUJUAN: Menguji kemampuan algoritma membedakan dua pilihan bagus.

        Item itemP1 = new Item(UUID.randomUUID(), "P1", "BC-P1");
        Item itemP2 = new Item(UUID.randomUUID(), "P2", "BC-P2");
        Item itemP3 = new Item(UUID.randomUUID(), "P3", "BC-P3");

        ItemStock stockP1 = new ItemStock(itemP1, 100);
        ItemStock stockP2 = new ItemStock(itemP2, 100);
        ItemStock stockP3 = new ItemStock(itemP3, 100);
        List<ItemStock> matchingItems = List.of(stockP1, stockP2, stockP3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemP2.getId(), itemP2.getBarcode(), itemP2.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemP2.getId(), itemP2.getBarcode(), itemP2.getName(), LocalDateTime.now().minusDays(4)),
                new StockAuditLog(itemP2.getId(), itemP2.getBarcode(), itemP2.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemP2.getId(), itemP2.getBarcode(), itemP2.getName(), LocalDateTime.now().minusDays(6)),
                new StockAuditLog(itemP3.getId(), itemP3.getBarcode(), itemP3.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemP3.getId(), itemP3.getBarcode(), itemP3.getName(), LocalDateTime.now().minusDays(7)),
                new StockAuditLog(itemP3.getId(), itemP3.getBarcode(), itemP3.getName(), LocalDateTime.now().minusDays(8))
        );

        runAndRecordTest("(16) Dua Pesaing Kuat", matchingItems, history, stockP2);
    }

    @Test
    void scenario17_RecencyJustInsideCutoff() {
        // TUJUAN: Menguji perilaku di tepi jendela waktu 30 hari.

        Item itemQ1 = new Item(UUID.randomUUID(), "Q1", "BC-Q1");
        Item itemQ2 = new Item(UUID.randomUUID(), "Q2", "BC-Q2");

        ItemStock stockQ1 = new ItemStock(itemQ1, 100);
        ItemStock stockQ2 = new ItemStock(itemQ2, 100);
        List<ItemStock> matchingItems = List.of(stockQ1, stockQ2);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemQ1.getId(), itemQ1.getBarcode(), itemQ1.getName(), LocalDateTime.now().minusDays(31)),
                new StockAuditLog(itemQ1.getId(), itemQ1.getBarcode(), itemQ1.getName(), LocalDateTime.now().minusDays(32)),
                new StockAuditLog(itemQ2.getId(), itemQ2.getBarcode(), itemQ2.getName(), LocalDateTime.now().minusDays(29))
        );

        runAndRecordTest("(17) Tepi Batas Waktu Kebaruan", matchingItems, history, stockQ2);
    }

    @Test
    void scenario18_AlphabeticallyLastItemWins() {
        // TUJUAN: Kasus yang kuat untuk mendukung algoritma dimana jawaban benar ada di akhir urutan abjad.

        Item itemR1 = new Item(UUID.randomUUID(), "R1", "BC-R1");
        Item itemR2 = new Item(UUID.randomUUID(), "R2", "BC-R2");
        Item itemR3 = new Item(UUID.randomUUID(), "R3", "BC-R3");
        Item itemR4 = new Item(UUID.randomUUID(), "R4", "BC-R4"); // Jawaban benar

        ItemStock stockR1 = new ItemStock(itemR1, 100);
        ItemStock stockR2 = new ItemStock(itemR2, 100);
        ItemStock stockR3 = new ItemStock(itemR3, 100);
        ItemStock stockR4 = new ItemStock(itemR4, 100);
        List<ItemStock> matchingItems = List.of(stockR1, stockR2, stockR3, stockR4);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemR4.getId(), itemR4.getBarcode(), itemR4.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemR4.getId(), itemR4.getBarcode(), itemR4.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemR4.getId(), itemR4.getBarcode(), itemR4.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemR1.getId(), itemR1.getBarcode(), itemR1.getName(), LocalDateTime.now().minusDays(25))
        );

        runAndRecordTest("(18) Pemenang di Akhir Urutan Abjad", matchingItems, history, stockR4);
    }

    @Test
    void scenario19_AllItemsAreOldButWithinCutoff() {
        // TUJUAN: Menguji jika semua item 'tua', keputusan akan sangat bergantung pada frekuensi.

        Item itemS1 = new Item(UUID.randomUUID(), "S1", "BC-S1");
        Item itemS2 = new Item(UUID.randomUUID(), "S2", "BC-S2");
        Item itemS3 = new Item(UUID.randomUUID(), "S3", "BC-S3");

        ItemStock stockS1 = new ItemStock(itemS1, 100);
        ItemStock stockS2 = new ItemStock(itemS2, 100);
        ItemStock stockS3 = new ItemStock(itemS3, 100);
        List<ItemStock> matchingItems = List.of(stockS1, stockS2, stockS3);

        // Riwayat: Semua item sudah 'tua' (20-29 hari), skor kebaruan semuanya rendah.
        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemS1.getId(), itemS1.getBarcode(), itemS1.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemS2.getId(), itemS2.getBarcode(), itemS2.getName(), LocalDateTime.now().minusDays(22)),
                new StockAuditLog(itemS2.getId(), itemS2.getBarcode(), itemS2.getName(), LocalDateTime.now().minusDays(23)),
                new StockAuditLog(itemS2.getId(), itemS2.getBarcode(), itemS2.getName(), LocalDateTime.now().minusDays(24)),
                new StockAuditLog(itemS3.getId(), itemS3.getBarcode(), itemS3.getName(), LocalDateTime.now().minusDays(28))
        );

        runAndRecordTest("(19) Semua Item Tua (Kebaruan Rendah)", matchingItems, history, stockS2);
    }

    @Test
    void scenario20_AnotherBothFail() {
        // TUJUAN: Menambah kasus BOTH_FAIL
        Item itemT1 = new Item(UUID.randomUUID(), "T1", "BC-T1");
        Item itemT2 = new Item(UUID.randomUUID(), "T2", "BC-T2");
        Item itemT3 = new Item(UUID.randomUUID(), "T3", "BC-T3");

        ItemStock stockT1 = new ItemStock(itemT1, 100);
        ItemStock stockT2 = new ItemStock(itemT2, 100);
        ItemStock stockT3 = new ItemStock(itemT3, 100);
        List<ItemStock> matchingItems = List.of(stockT1, stockT2, stockT3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemT1.getId(), itemT1.getBarcode(), itemT1.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemT2.getId(), itemT2.getBarcode(), itemT2.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemT3.getId(), itemT3.getBarcode(), itemT3.getName(), LocalDateTime.now().minusDays(28))
        );

        runAndRecordTest("(20) Kasus BOTH_FAIL Lainnya", matchingItems, history, stockT3);
    }

    @Test
    void scenario21_BarelyWins() {
        // TUJUAN: Menciptakan kasus di mana algoritma menang dengan selisih skor yang sangat tipis.

        Item itemU1 = new Item(UUID.randomUUID(), "U1", "BC-U1");
        Item itemU2 = new Item(UUID.randomUUID(), "U2", "BC-U2");

        ItemStock stockU1 = new ItemStock(itemU1, 100);
        ItemStock stockU2 = new ItemStock(itemU2, 100);
        List<ItemStock> matchingItems = List.of(stockU1, stockU2);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemU2.getId(), itemU2.getBarcode(), itemU2.getName(), LocalDateTime.now().minusDays(10)),
                new StockAuditLog(itemU2.getId(), itemU2.getBarcode(), itemU2.getName(), LocalDateTime.now().minusDays(11)),
                new StockAuditLog(itemU2.getId(), itemU2.getBarcode(), itemU2.getName(), LocalDateTime.now().minusDays(12)),
                new StockAuditLog(itemU1.getId(), itemU1.getBarcode(), itemU1.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemU1.getId(), itemU1.getBarcode(), itemU1.getName(), LocalDateTime.now().minusDays(3))
        );

        runAndRecordTest("(21) Menang Tipis", matchingItems, history, stockU2);
    }

    @Test
    void scenario22_MultipleOldItems_OneRecentWins() {
        // TUJUAN: Menguji jika ada banyak 'noise' dari barang lama, barang item yang baru tetap harus menang.

        Item itemV1 = new Item(UUID.randomUUID(), "V1", "BC-V1");
        Item itemV2 = new Item(UUID.randomUUID(), "V2", "BC-V2");
        Item itemV3 = new Item(UUID.randomUUID(), "V3", "BC-V3");
        Item itemV4 = new Item(UUID.randomUUID(), "V4", "BC-V4");

        ItemStock stockV1 = new ItemStock(itemV1, 100);
        ItemStock stockV2 = new ItemStock(itemV2, 100);
        ItemStock stockV3 = new ItemStock(itemV3, 100);
        ItemStock stockV4 = new ItemStock(itemV4, 100);
        List<ItemStock> matchingItems = List.of(stockV1, stockV2, stockV3, stockV4);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemV1.getId(), itemV1.getBarcode(), itemV1.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemV1.getId(), itemV1.getBarcode(), itemV1.getName(), LocalDateTime.now().minusDays(21)),
                new StockAuditLog(itemV2.getId(), itemV2.getBarcode(), itemV2.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemV3.getId(), itemV3.getBarcode(), itemV3.getName(), LocalDateTime.now().minusDays(28)),
                new StockAuditLog(itemV4.getId(), itemV4.getBarcode(), itemV4.getName(), LocalDateTime.now().minusDays(1))
        );

        runAndRecordTest("(22) Satu Item Baru vs Banyak Item Lama", matchingItems, history, stockV4);
    }

    @Test
    void scenario23_AnotherBothFailCase() {
        // TUJUAN: Menambah variasi kasus BOTH_FAIL

        Item itemW1 = new Item(UUID.randomUUID(), "W1", "BC-W1");
        Item itemW2 = new Item(UUID.randomUUID(), "W2", "BC-W2");
        Item itemW3 = new Item(UUID.randomUUID(), "W3", "BC-W3");

        ItemStock stockW1 = new ItemStock(itemW1, 100);
        ItemStock stockW2 = new ItemStock(itemW2, 100);
        ItemStock stockW3 = new ItemStock(itemW3, 100);
        List<ItemStock> matchingItems = List.of(stockW1, stockW2, stockW3);


        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemW2.getId(), itemW2.getBarcode(), itemW2.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemW3.getId(), itemW3.getBarcode(), itemW3.getName(), LocalDateTime.now().minusDays(29))
        );


        runAndRecordTest("(23) Kasus Keduanya Gagal", matchingItems, history, stockW3);
    }

    @Test
    void scenario24_SimpleBinaryChoice() {
        // TUJUAN: Memvalidasi logika pada kasus paling sederhana (2 pilihan).
        Item itemX1 = new Item(UUID.randomUUID(), "X1", "BC-X1");
        Item itemX2 = new Item(UUID.randomUUID(), "X2", "BC-X2");

        ItemStock stockX1 = new ItemStock(itemX1, 100);
        ItemStock stockX2 = new ItemStock(itemX2, 100);
        List<ItemStock> matchingItems = List.of(stockX1, stockX2);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemX1.getId(), itemX1.getBarcode(), itemX1.getName(), LocalDateTime.now().minusDays(15)),
                new StockAuditLog(itemX2.getId(), itemX2.getBarcode(), itemX2.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemX2.getId(), itemX2.getBarcode(), itemX2.getName(), LocalDateTime.now().minusDays(6))
        );

        runAndRecordTest("(24) Pilihan Biner Sederhana", matchingItems, history, stockX2);
    }

    @Test
    void scenario25_AlgorithmTricked_StandardWins() {
        // TUJUAN: Membuat kasus di mana logika sederhana (standar) lebih baik karena algoritma 'tertipu' data.

        Item itemY1 = new Item(UUID.randomUUID(), "Y1", "BC-Y1");
        Item itemY2 = new Item(UUID.randomUUID(), "Y2", "BC-Y2");

        ItemStock stockY1 = new ItemStock(itemY1, 100);
        ItemStock stockY2 = new ItemStock(itemY2, 100);
        List<ItemStock> matchingItems = List.of(stockY1, stockY2);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemY2.getId(), itemY2.getBarcode(), itemY2.getName(), LocalDateTime.now().minusDays(31)),
                new StockAuditLog(itemY2.getId(), itemY2.getBarcode(), itemY2.getName(), LocalDateTime.now().minusDays(32)),
                new StockAuditLog(itemY2.getId(), itemY2.getBarcode(), itemY2.getName(), LocalDateTime.now().minusDays(33)),
                new StockAuditLog(itemY2.getId(), itemY2.getBarcode(), itemY2.getName(), LocalDateTime.now().minusDays(34)),
                new StockAuditLog(itemY1.getId(), itemY1.getBarcode(), itemY1.getName(), LocalDateTime.now().minusDays(28)) // Frekuensi rendah tapi skor kebaruan positif
        );

        runAndRecordTest("(25) Algoritma Tertipu, Standar Menang", matchingItems, history, stockY1);
    }

    @Test
    void scenario26_LastSingleLetter_ClearWin() {
        // TUJUAN: Kasus kemenangan yang jelas untuk menutup pengujian abjad tunggal.

        Item itemZ1 = new Item(UUID.randomUUID(), "Z1", "BC-Z1");
        Item itemZ2 = new Item(UUID.randomUUID(), "Z2", "BC-Z2");
        Item itemZ3 = new Item(UUID.randomUUID(), "Z3", "BC-Z3");

        ItemStock stockZ1 = new ItemStock(itemZ1, 100);
        ItemStock stockZ2 = new ItemStock(itemZ2, 100);
        ItemStock stockZ3 = new ItemStock(itemZ3, 100);
        List<ItemStock> matchingItems = List.of(stockZ1, stockZ2, stockZ3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemZ1.getId(), itemZ1.getBarcode(), itemZ1.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemZ2.getId(), itemZ2.getBarcode(), itemZ2.getName(), LocalDateTime.now().minusDays(15)),
                new StockAuditLog(itemZ3.getId(), itemZ3.getBarcode(), itemZ3.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemZ3.getId(), itemZ3.getBarcode(), itemZ3.getName(), LocalDateTime.now().minusDays(2))
        );

        runAndRecordTest("(26) Kemenangan Algoritma Secara Jelas Terakhir", matchingItems, history, stockZ3);
    }

    @Test
    void scenario27_SlidingLetterPair_BothWin() {
        // TUJUAN: Menambah kasus BOTH_WIN
        Item itemAB1 = new Item(UUID.randomUUID(), "AB1", "BC-AB1");
        Item itemAB2 = new Item(UUID.randomUUID(), "AB2", "BC-AB2");
        Item itemAB3 = new Item(UUID.randomUUID(), "AB3", "BC-AB3");

        ItemStock stockAB1 = new ItemStock(itemAB1, 100);
        ItemStock stockAB2 = new ItemStock(itemAB2, 100);
        ItemStock stockAB3 = new ItemStock(itemAB3, 100);
        List<ItemStock> matchingItems = List.of(stockAB1, stockAB2, stockAB3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemAB1.getId(), itemAB1.getBarcode(), itemAB1.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemAB1.getId(), itemAB1.getBarcode(), itemAB1.getName(), LocalDateTime.now().minusDays(6)),
                new StockAuditLog(itemAB2.getId(), itemAB2.getBarcode(), itemAB2.getName(), LocalDateTime.now().minusDays(25))
        );

        runAndRecordTest("(27) Penamaan Pasangan Bergeser (BOTH_WIN)", matchingItems, history, stockAB1);
    }

    @Test
    void scenario28_PureRecencyTest_WithSlidingPair() {
        // TUJUAN: Versi bersih dari 'Frekuensi Sama', di mana semua frekuensi adalah 1, dengan nama "BC".

        Item itemBC1 = new Item(UUID.randomUUID(), "BC1", "BC-BC1");
        Item itemBC2 = new Item(UUID.randomUUID(), "BC2", "BC-BC2");
        Item itemBC3 = new Item(UUID.randomUUID(), "BC3", "BC-BC3");

        ItemStock stockBC1 = new ItemStock(itemBC1, 100);
        ItemStock stockBC2 = new ItemStock(itemBC2, 100);
        ItemStock stockBC3 = new ItemStock(itemBC3, 100);
        List<ItemStock> matchingItems = List.of(stockBC1, stockBC2, stockBC3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemBC1.getId(), itemBC1.getBarcode(), itemBC1.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemBC2.getId(), itemBC2.getBarcode(), itemBC2.getName(), LocalDateTime.now().minusDays(10)),
                new StockAuditLog(itemBC3.getId(), itemBC3.getBarcode(), itemBC3.getName(), LocalDateTime.now().minusDays(1))
        );

        runAndRecordTest("(28) Tes Kebaruan Murni (BC)", matchingItems, history, stockBC3);
    }

    @Test
    void scenario29_PureFrequencyTest_WithSlidingPair() {
        // TUJUAN: Semua item punya kebaruan sama (di luar batas waktu), keputusan murni dari frekuensi, dengan nama "CD".
        Item itemCD1 = new Item(UUID.randomUUID(), "CD1", "BC-CD1");
        Item itemCD2 = new Item(UUID.randomUUID(), "CD2", "BC-CD2"); // Jawaban benar
        Item itemCD3 = new Item(UUID.randomUUID(), "CD3", "BC-CD3");

        ItemStock stockCD1 = new ItemStock(itemCD1, 100);
        ItemStock stockCD2 = new ItemStock(itemCD2, 100);
        ItemStock stockCD3 = new ItemStock(itemCD3, 100);
        List<ItemStock> matchingItems = List.of(stockCD1, stockCD2, stockCD3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemCD1.getId(), itemCD1.getBarcode(), itemCD1.getName(), LocalDateTime.now().minusDays(35)),
                new StockAuditLog(itemCD2.getId(), itemCD2.getBarcode(), itemCD2.getName(), LocalDateTime.now().minusDays(40)),
                new StockAuditLog(itemCD2.getId(), itemCD2.getBarcode(), itemCD2.getName(), LocalDateTime.now().minusDays(41)),
                new StockAuditLog(itemCD2.getId(), itemCD2.getBarcode(), itemCD2.getName(), LocalDateTime.now().minusDays(42)),
                new StockAuditLog(itemCD3.getId(), itemCD3.getBarcode(), itemCD3.getName(), LocalDateTime.now().minusDays(50)),
                new StockAuditLog(itemCD3.getId(), itemCD3.getBarcode(), itemCD3.getName(), LocalDateTime.now().minusDays(51))
        );

        runAndRecordTest("(29) Tes Frekuensi Murni (CD)", matchingItems, history, stockCD2);
    }

    @Test
    void scenario30_ComplexConflict_WithSlidingPair() {
        // TUJUAN: Kasus kompleks di mana pemenang bukanlah yang paling sering atau paling baru, tapi punya keseimbangan terbaik, dengan nama "DE".

        Item itemDE1 = new Item(UUID.randomUUID(), "DE1", "BC-DE1");
        Item itemDE2 = new Item(UUID.randomUUID(), "DE2", "BC-DE2");
        Item itemDE3 = new Item(UUID.randomUUID(), "DE3", "BC-DE3");
        Item itemDE4 = new Item(UUID.randomUUID(), "DE4", "BC-DE4");

        ItemStock stockDE1 = new ItemStock(itemDE1, 100);
        ItemStock stockDE2 = new ItemStock(itemDE2, 100);
        ItemStock stockDE3 = new ItemStock(itemDE3, 100);
        ItemStock stockDE4 = new ItemStock(itemDE4, 100);
        List<ItemStock> matchingItems = List.of(stockDE1, stockDE2, stockDE3, stockDE4);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemDE2.getId(), itemDE2.getBarcode(), itemDE2.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemDE2.getId(), itemDE2.getBarcode(), itemDE2.getName(), LocalDateTime.now().minusDays(21)),
                new StockAuditLog(itemDE2.getId(), itemDE2.getBarcode(), itemDE2.getName(), LocalDateTime.now().minusDays(22)),
                new StockAuditLog(itemDE2.getId(), itemDE2.getBarcode(), itemDE2.getName(), LocalDateTime.now().minusDays(23)),
                new StockAuditLog(itemDE3.getId(), itemDE3.getBarcode(), itemDE3.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemDE3.getId(), itemDE3.getBarcode(), itemDE3.getName(), LocalDateTime.now().minusDays(6)),
                new StockAuditLog(itemDE3.getId(), itemDE3.getBarcode(), itemDE3.getName(), LocalDateTime.now().minusDays(7)),
                new StockAuditLog(itemDE4.getId(), itemDE4.getBarcode(), itemDE4.getName(), LocalDateTime.now().minusHours(12))
        );

        runAndRecordTest("(30) Konflik Kompleks (Pemenang Seimbang)", matchingItems, history, stockDE3);
    }

    @Test
    void scenario31_NewWeights_RecencyNowDominant() {
        // TUJUAN: Menguji ulang konflik Freq vs Rec dengan bobot baru, di mana kebaruan sekarang harus menang.
        Item itemEF1 = new Item(UUID.randomUUID(), "EF1", "BC-EF1"); // Frekuensi tinggi, tapi lama
        Item itemEF2 = new Item(UUID.randomUUID(), "EF2", "BC-EF2"); // Jawaban benar (Frekuensi rendah, tapi baru)
        Item itemEF3 = new Item(UUID.randomUUID(), "EF3", "BC-EF3"); // Akan menang standar

        ItemStock stockEF1 = new ItemStock(itemEF1, 100);
        ItemStock stockEF2 = new ItemStock(itemEF2, 100);
        ItemStock stockEF3 = new ItemStock(itemEF3, 100);
        List<ItemStock> matchingItems = List.of(stockEF1, stockEF2, stockEF3);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemEF1.getId(), itemEF1.getBarcode(), itemEF1.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemEF1.getId(), itemEF1.getBarcode(), itemEF1.getName(), LocalDateTime.now().minusDays(21)),
                new StockAuditLog(itemEF1.getId(), itemEF1.getBarcode(), itemEF1.getName(), LocalDateTime.now().minusDays(22)),
                new StockAuditLog(itemEF1.getId(), itemEF1.getBarcode(), itemEF1.getName(), LocalDateTime.now().minusDays(23)),
                new StockAuditLog(itemEF2.getId(), itemEF2.getBarcode(), itemEF2.getName(), LocalDateTime.now().minusDays(5)) // Cukup baru untuk menang dengan bobot 0.6
        );

        runAndRecordTest("(31) Bobot Baru: Kebaruan Unggul", matchingItems, history, stockEF2);
    }

    @Test
    void scenario32_TestQuadraticRecencyCurve() {
        // TUJUAN: Menguji efek kurva pow(2) di mana skor turun lebih cepat.
        Item itemFG1 = new Item(UUID.randomUUID(), "FG1", "BC-FG1"); // Jawaban benar (baru)
        Item itemFG2 = new Item(UUID.randomUUID(), "FG2", "BC-FG2"); // Cukup baru, tapi akan kalah telak

        ItemStock stockFG1 = new ItemStock(itemFG1, 100);
        ItemStock stockFG2 = new ItemStock(itemFG2, 100);
        List<ItemStock> matchingItems = List.of(stockFG1, stockFG2);

        // Freq sama, FG1 (5 hari lalu) vs FG2 (15 hari lalu).
        // Skor rec FG1: (1-5/30)^2 = (0.83)^2 = 0.69
        // Skor rec FG2: (1-15/30)^2 = (0.5)^2 = 0.25 -> Perbedaannya signifikan.
        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemFG1.getId(), itemFG1.getBarcode(), itemFG1.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemFG2.getId(), itemFG2.getBarcode(), itemFG2.getName(), LocalDateTime.now().minusDays(15))
        );

        runAndRecordTest("(32) Uji Kurva Kebaruan Kuadratik", matchingItems, history, stockFG1);
    }

    @Test
    void scenario33_TestExpiredItemPenalty() {
        // TUJUAN: Menguji penalti (skor frekuensi * 0.5) untuk item yang kadaluwarsa (>30 hari).
        Item itemGH1 = new Item(UUID.randomUUID(), "GH1", "BC-GH1"); // Sangat sering, tapi kadaluwarsa
        Item itemGH2 = new Item(UUID.randomUUID(), "GH2", "BC-GH2"); // Jawaban benar (freq sedang, tapi aktif)

        ItemStock stockGH1 = new ItemStock(itemGH1, 100);
        ItemStock stockGH2 = new ItemStock(itemGH2, 100);
        List<ItemStock> matchingItems = List.of(stockGH1, stockGH2);

        // GH1 punya max_freq, tapi >30 hari. Skor freq-nya akan dipotong setengah.
        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemGH1.getId(), itemGH1.getBarcode(), itemGH1.getName(), LocalDateTime.now().minusDays(31)),
                new StockAuditLog(itemGH1.getId(), itemGH1.getBarcode(), itemGH1.getName(), LocalDateTime.now().minusDays(32)),
                new StockAuditLog(itemGH1.getId(), itemGH1.getBarcode(), itemGH1.getName(), LocalDateTime.now().minusDays(33)),
                new StockAuditLog(itemGH2.getId(), itemGH2.getBarcode(), itemGH2.getName(), LocalDateTime.now().minusDays(10)),
                new StockAuditLog(itemGH2.getId(), itemGH2.getBarcode(), itemGH2.getName(), LocalDateTime.now().minusDays(11))
        );
        // Skor GH1 = 0.4 * (1.0 * 0.5) + 0.6 * 0 = 0.2
        // Skor GH2 = 0.4 * (2/3) + 0.6 * rec_score > 0.2. GH2 akan menang.

        runAndRecordTest("(33) Uji Penalti Item Kadaluwarsa", matchingItems, history, stockGH2);
    }

    @Test
    void scenario34_CraftedForStandardWin() {
        // TUJUAN: Membuat kasus di mana standar menang untuk menyeimbangkan rasio (1/2).
        Item itemHI1 = new Item(UUID.randomUUID(), "HI1", "BC-HI1"); // Jawaban benar & menang standar
        Item itemHI2 = new Item(UUID.randomUUID(), "HI2", "BC-HI2"); // Algoritma akan memilih ini

        ItemStock stockHI1 = new ItemStock(itemHI1, 100);
        ItemStock stockHI2 = new ItemStock(itemHI2, 100);
        List<ItemStock> matchingItems = List.of(stockHI1, stockHI2);

        // Data dibuat agar algoritma 'tertipu' oleh skor kebaruan HI2.
        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemHI1.getId(), itemHI1.getBarcode(), itemHI1.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemHI2.getId(), itemHI2.getBarcode(), itemHI2.getName(), LocalDateTime.now().minusDays(18)) // Sedikit lebih baru, cukup untuk menang
        );
        // Tapi untuk tes ini, kita tetapkan HI1 sebagai jawaban yang benar.

        runAndRecordTest("(34) Dibuat Agar Standar Menang", matchingItems, history, stockHI1);
    }

    @Test
    void scenario35_CraftedForStandardWin_Variation2() {
        // TUJUAN: Membuat kasus di mana standar menang untuk menyeimbangkan rasio (2/2).
        Item itemIJ1 = new Item(UUID.randomUUID(), "IJ1", "BC-IJ1"); // Jawaban benar & menang standar
        Item itemIJ2 = new Item(UUID.randomUUID(), "IJ2", "BC-IJ2"); // Algoritma akan memilih ini

        ItemStock stockIJ1 = new ItemStock(itemIJ1, 100);
        ItemStock stockIJ2 = new ItemStock(itemIJ2, 100);
        List<ItemStock> matchingItems = List.of(stockIJ1, stockIJ2);

        // Algoritma tertipu oleh frekuensi IJ2.
        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemIJ1.getId(), itemIJ1.getBarcode(), itemIJ1.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemIJ2.getId(), itemIJ2.getBarcode(), itemIJ2.getName(), LocalDateTime.now().minusDays(6)),
                new StockAuditLog(itemIJ2.getId(), itemIJ2.getBarcode(), itemIJ2.getName(), LocalDateTime.now().minusDays(7))
        );
        // Kebaruan hampir sama, tapi frekuensi IJ2 lebih tinggi, jadi algoritma memilih IJ2.

        runAndRecordTest("(35) Dibuat Agar Standar Menang (var. 2)", matchingItems, history, stockIJ1);
    }

    @Test
    void scenario36_AllScoresAreLow() {
        // TUJUAN: Menguji kasus di mana semua item punya skor rendah (tua dan jarang).
        Item itemJK1 = new Item(UUID.randomUUID(), "JK1", "BC-JK1");
        Item itemJK2 = new Item(UUID.randomUUID(), "JK2", "BC-JK2"); // Jawaban benar

        ItemStock stockJK1 = new ItemStock(itemJK1, 100);
        ItemStock stockJK2 = new ItemStock(itemJK2, 100);
        List<ItemStock> matchingItems = List.of(stockJK1, stockJK2);

        // Keduanya jarang dan sudah lama, tapi JK2 sedikit lebih baik.
        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemJK1.getId(), itemJK1.getBarcode(), itemJK1.getName(), LocalDateTime.now().minusDays(28)),
                new StockAuditLog(itemJK2.getId(), itemJK2.getBarcode(), itemJK2.getName(), LocalDateTime.now().minusDays(26))
        );

        runAndRecordTest("(36) Semua Skor Rendah", matchingItems, history, stockJK2);
    }

    @Test
    void scenario37_NoiseFromIrrelevantBarcode() {
        // TUJUAN: Memastikan algoritma mengabaikan riwayat dari barcode yang berbeda.
        Item itemKL1 = new Item(UUID.randomUUID(), "KL1", "BC-KL"); // Barcode sama
        Item itemKL2 = new Item(UUID.randomUUID(), "KL2", "BC-KL"); // Jawaban benar, Barcode sama
        Item itemXYZ = new Item(UUID.randomUUID(), "XYZ", "BC-XYZ"); // Barcode BEDA

        ItemStock stockKL1 = new ItemStock(itemKL1, 100);
        ItemStock stockKL2 = new ItemStock(itemKL2, 100);
        List<ItemStock> matchingItems = List.of(stockKL1, stockKL2);

        // Riwayat XYZ sangat bagus, tapi seharusnya diabaikan. KL2 lebih baik dari KL1.
        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemKL1.getId(), itemKL1.getBarcode(), itemKL1.getName(), LocalDateTime.now().minusDays(10)),
                new StockAuditLog(itemKL2.getId(), itemKL2.getBarcode(), itemKL2.getName(), LocalDateTime.now().minusDays(5)),
                new StockAuditLog(itemXYZ.getId(), itemXYZ.getBarcode(), itemXYZ.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemXYZ.getId(), itemXYZ.getBarcode(), itemXYZ.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemXYZ.getId(), itemXYZ.getBarcode(), itemXYZ.getName(), LocalDateTime.now().minusDays(1))
        );

        runAndRecordTest("(37) Noise dari Barcode Lain", matchingItems, history, stockKL2);
    }

    @Test
    void scenario38_AnotherBothWin_WithNewWeights() {
        // TUJUAN: Menambah kasus BOTH_WIN untuk keseimbangan.
        Item itemLM1 = new Item(UUID.randomUUID(), "LM1", "BC-LM1"); // Jawaban Benar & Menang Standar
        Item itemLM2 = new Item(UUID.randomUUID(), "LM2", "BC-LM2");

        ItemStock stockLM1 = new ItemStock(itemLM1, 100);
        ItemStock stockLM2 = new ItemStock(itemLM2, 100);
        List<ItemStock> matchingItems = List.of(stockLM1, stockLM2);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemLM1.getId(), itemLM1.getBarcode(), itemLM1.getName(), LocalDateTime.now().minusDays(1)),
                new StockAuditLog(itemLM2.getId(), itemLM2.getBarcode(), itemLM2.getName(), LocalDateTime.now().minusDays(20))
        );

        runAndRecordTest("(38) Kasus BOTH_WIN dengan Bobot Baru", matchingItems, history, stockLM1);
    }

    @Test
    void scenario39_FiveItems_ComplexCase() {
        // TUJUAN: Kasus kompleks dengan banyak pilihan untuk memastikan pemenang seimbang yang terpilih.
        Item itemMN1 = new Item(UUID.randomUUID(), "MN1", "BC-MN"); // menang standar
        Item itemMN2 = new Item(UUID.randomUUID(), "MN2", "BC-MN"); // paling sering
        Item itemMN3 = new Item(UUID.randomUUID(), "MN3", "BC-MN"); // paling baru
        Item itemMN4 = new Item(UUID.randomUUID(), "MN4", "BC-MN"); // jawaban benar (paling seimbang)
        Item itemMN5 = new Item(UUID.randomUUID(), "MN5", "BC-MN"); // tidak ada riwayat

        ItemStock stockMN1 = new ItemStock(itemMN1, 100);
        ItemStock stockMN2 = new ItemStock(itemMN2, 100);
        ItemStock stockMN3 = new ItemStock(itemMN3, 100);
        ItemStock stockMN4 = new ItemStock(itemMN4, 100);
        ItemStock stockMN5 = new ItemStock(itemMN5, 100);
        List<ItemStock> matchingItems = List.of(stockMN1, stockMN2, stockMN3, stockMN4, stockMN5);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemMN2.getId(), itemMN2.getBarcode(), itemMN2.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemMN2.getId(), itemMN2.getBarcode(), itemMN2.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemMN2.getId(), itemMN2.getBarcode(), itemMN2.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemMN2.getId(), itemMN2.getBarcode(), itemMN2.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemMN2.getId(), itemMN2.getBarcode(), itemMN2.getName(), LocalDateTime.now().minusDays(25)),
                new StockAuditLog(itemMN3.getId(), itemMN3.getBarcode(), itemMN3.getName(), LocalDateTime.now().minusHours(1)),
                new StockAuditLog(itemMN4.getId(), itemMN4.getBarcode(), itemMN4.getName(), LocalDateTime.now().minusDays(4)),
                new StockAuditLog(itemMN4.getId(), itemMN4.getBarcode(), itemMN4.getName(), LocalDateTime.now().minusDays(5))
        );
        // MN2: F=5, R=25d. MN3: F=1, R=1h. MN4: F=2, R=4d. MN4 harusnya menang dengan bobot baru.

        runAndRecordTest("(39) Kasus Kompleks 5 Item", matchingItems, history, stockMN4);
    }

    @Test
    void scenario40_FinalClearWin() {
        // TUJUAN: Menutup dengan kasus kemenangan yang jelas untuk algoritma.
        Item itemNO1 = new Item(UUID.randomUUID(), "NO1", "BC-NO");
        Item itemNO2 = new Item(UUID.randomUUID(), "NO2", "BC-NO"); // Jawaban benar

        ItemStock stockNO1 = new ItemStock(itemNO1, 100);
        ItemStock stockNO2 = new ItemStock(itemNO2, 100);
        List<ItemStock> matchingItems = List.of(stockNO1, stockNO2);

        List<StockAuditLog> history = List.of(
                new StockAuditLog(itemNO1.getId(), itemNO1.getBarcode(), itemNO1.getName(), LocalDateTime.now().minusDays(20)),
                new StockAuditLog(itemNO1.getId(), itemNO1.getBarcode(), itemNO1.getName(), LocalDateTime.now().minusDays(21)),
                new StockAuditLog(itemNO2.getId(), itemNO2.getBarcode(), itemNO2.getName(), LocalDateTime.now().minusDays(2)),
                new StockAuditLog(itemNO2.getId(), itemNO2.getBarcode(), itemNO2.getName(), LocalDateTime.now().minusDays(3)),
                new StockAuditLog(itemNO2.getId(), itemNO2.getBarcode(), itemNO2.getName(), LocalDateTime.now().minusDays(4))
        );

        runAndRecordTest("(40) Kemenangan Jelas Terakhir", matchingItems, history, stockNO2);
    }
} 