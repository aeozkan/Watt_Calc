# WattBench — Gelecek Özellikler (Future Features)

Bu doküman, WattBench uygulamasına gelecekte eklenecek olan planlanmış yenilikleri ve teknik konseptleri içerir.

---

### 1. 🏆 Şarj Performans Puanı (WattBench Score / 100)
- **Nasıl Çalışır?:** Benchmark testi tamamlandığında, adaptör + kablo kombinasyonunu Li-ion CC/CV pil fazları ve donanım protokollerine göre adil şekilde 100 üzerinden puanlar.
- **Detaylı Spesifikasyon:** [sarj-benchmark-algoritma-spec.md](file:///F:/Users/aeozk/AndroidStudioProjects/Watt%20Calculator/sarj-benchmark-algoritma-spec.md)

---

### 2. 📸 Şık İnceleme Görseli Paylaşma (Share Card / Graphic)
- **Nasıl Çalışır?:** Benchmark sonuç kartında bulunan "Paylaş" butonuyla, test sonuçlarını (Zirve Watt, Ortalama Watt, Grafik, Puan ve Adaptör/Kablo Bilgisi) neon temalı tek bir PNG görseli olarak üretir ve sosyal medyada paylaşılmasını sağlar.

---

### 3. 🔴 Sıcaklık & Aşırı Isınma Uyarısı (Thermal Guard)
- **Nasıl Çalışır?:** Canlı telemetri ve arka plan ölçümlerinde batarya sıcaklığı güvenli eşiği (örn. 38°C veya 45°C) aştığında kullanıcıyı sesli/görsel olarak uyarır ve pil sağlığını korumayı hedefler.

---

### 4. ℹ️ Benchmark Başlatma Bilgilendirme Notu (Arka Plan Çalışma Bilgisi)
- **Nasıl Çalışır?:** Kullanıcı Benchmark başlat butonuna bastığında, uygulamanın mevcut haliyle arka planda neden ve nasıl çalışabileceğini (pil optimizasyonları, sistem kısıtlamaları ve ölçüm mantığı) anlatan kısa bir bilgilendirme notu gösterir. Böylece kullanıcının güvenini kazanmayı ve beklentiyi doğru yönetmeyi sağlar.

---

### 5. 🔌 %100 Şarj Tamamlanma & Batarya Koruma Analizi (Full Charge & Cut-Off Analytics)
- **Nasıl Çalışır?:** Telefon şarjdayken başlatılan benchmark testinde pil %100 doluma ulaştığında ve ardından cihazın kendi donanımsal/yazılımsal pil koruma sistemi devreye girip akımı tamamen keserek (0 Watt) çizdiğinde bu süreci analiz eder. Benchmark sonucunda:
  1. **%100 Doluma Ulaşma Süresi** (Saat ve dakika cinsinden),
  2. **%100 Olduktan Sonra Prizde/Kabloda Kalma Süresi**,
  3. **Batarya Korumasının Akımı Tamamen Kesip 0 Watt'a İndirdiği An ve Koruma Süresi**
  detaylı bir zaman çizelgesi analizi olarak kullanıcıya sunulur.
