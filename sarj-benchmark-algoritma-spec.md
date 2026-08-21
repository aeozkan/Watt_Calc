# Şarj Kablosu/Adaptörü Performans Benchmark Algoritması — Nihai Spesifikasyon

## Amaç
Kullanıcının elindeki farklı adaptör + kablo kombinasyonlarını, telefonun o anki koşullarına (pil yüzdesi, sıcaklık) ve donanım/protokol seviyesine göre adil şekilde puanlayan, tek seferlik ve anlık ölçüme dayalı (uzun vadeli veri birikimi gerektirmeyen) bir benchmark sistemi.

## Tasarım İlkeleri
1. Hiçbir tahmini/uydurma düzeltme katsayısı kullanılmaz. Değişkenler (pil %, sıcaklık) katsayıyla telafi edilmez; dar pencerelerle ve filtrelerle kontrol altına alınır.
2. Referans tavan mümkün olduğunca **canlı ölçümle** belirlenir, statik veritabanı veya kullanıcı girişiyle değil.
3. Li-ion şarj eğrisinin CC (Sabit Akım) ve CV (Sabit Voltaj) fazları ayrıştırılır; sadece CC fazı puanlanır.
4. Sistem, protokol/donanım seviyesine göre kademeli çalışır — modern USB-PD/PPS'ten 10-15 yıllık negotiate'siz USB'ye kadar her seviyede dürüst bir referans noktası bulur.
5. Her sonuç, o testin ne kadar güvenilir olduğunu gösteren bir güven etiketiyle birlikte sunulur.

---

## 1. Ön Koşul Filtreleri

Test başlamadan önce kontrol edilir; koşullar sağlanmazsa test başlatılmaz veya kullanıcı uyarılır.

| Koşul | Sınır | Aksiyon |
|---|---|---|
| Pil yüzdesi | %20 – %50 arası | Aralık dışıysa test başlatılmaz |
| Batarya sıcaklığı (başlangıç) | 25°C – 35°C arası | Aralık dışıysa test başlatılmaz |

## 2. Protokol / Donanım Tespiti (Kademeli)

Test başında sırayla denenir, ilk uyan kademe kullanılır:

### Kademe 1 — Dijital Negotiate (USB-PD 2.0/3.0, PPS)
- Tespit: CC pinleri üzerinden PDO (Power Data Object) listesi alışverişi başarılı.
- Referans (`W_ref`): Negotiate edilen gerçek watt tavanı, canlı okunur.
- Etiket: `"USB-PD (PPS)"` / `"USB-PD"` — okunan gerçek değerle birlikte.
- Güven kademesi: **Tam** (referans doğrudan ölçülüyor).

### Kademe 2 — Proprietary Hızlı Şarj (QC 2.0/3.0, AFC, Pump Express vb.)
- Tespit: Dijital PDO yok, ancak sistem "Fast/Quick Charging" broadcast'i veriyor veya D+/D- voltaj kalıbı bu protokollerden birine uyuyor.
- Referans (`W_ref`): Testin ilk birkaç saniyesinde voltaj/akımın oturduğu **kararlı plato** ölçülür (örn. 9V × 1.67A ≈ 15W QC seviyesi).
- Etiket: `"Proprietary Hızlı Şarj (Tahmini: <protokol adı>)"`.
- Güven kademesi: **Bir kademe düşük** — referans beyan değil, davranışsal gözlem.

### Kademe 3 — Negotiate Yok / Standart USB (eski Micro-USB, 10-15 yıllık cihazlar)
- Tespit: Ne PDO ne de bilinen proprietary voltaj kalıbı bulunamadı; sabit 5V çıkış.
- Referans (`W_ref`): **Negotiate edilen tavan yerine telefonun donanımsal giriş limiti** kullanılır. Bu değer, cihazın USB port sınırından (Android'de örn. `usb_max_current` sysfs değeri veya `BatteryManager` üzerinden okunabilen donanım limiti) alınır — bulut/veritabanından değil, cihazın kendi raporundan.
- Etiket: `"Negotiate Protokolü Tespit Edilemedi — Referans: Cihaz Donanım Limiti"`.
- Güven kademesi: **İki kademe düşük** — referans canlı ölçülen bir anlaşma değil, statik donanım varsayımı.

> Not: Kademe 3'te "Gözlemlenen Kararlı Güç / Gözlemlenen Kararlı Güç" gibi payda-pay aynı kaynaktan gelen bir oran KULLANILMAZ (anlamsız %100'e yakın sonuç verir). Referans mutlaka bağımsız bir kaynaktan (donanım limiti) gelmelidir.

## 3. Faz Ayrıştırma (CC / CV)

- Testin ilk anından itibaren akım örneklenir.
- Akımın belirgin ve sürekli bir eğimle düşmeye başladığı nokta CV fazının başlangıcı sayılır ve o andan sonraki veri **puanlamaya dahil edilmez**.
- Yalnızca CC fazındaki veri ("temiz CC verisi") sonraki adımlarda kullanılır.

## 4. Anlık Throttling Filtresi

- Test sırasında batarya sıcaklığı 38°C'yi geçerse, o andan itibaren gelen veri örneklemden çıkarılır (cezalandırma/ödüllendirme katsayısı uygulanmaz — veri sadece dışlanır).
- Kullanıcıya not düşülür: kaç saniyelik verinin bu sebeple dışlandığı.

## 5. Skorlama (100 Puan Üzerinden)

Tüm hesaplamalar yalnızca "temiz CC verisi" üzerinden yapılır.

**A) Verimlilik Puanı (%50)**

```
Puan_1 = (Ortalama_W_temizCC / W_ref) × 50
```

**B) Kararlılık Puanı (%30)** — varyasyon katsayısı (CV = σ/μ) kullanılır, ham standart sapma değil:

```
CV = σ_W_temizCC / Ortalama_W_temizCC
Puan_2 = 30 − (CV × 100 × Çarpan)
```

(Çarpan, pilot testlerle kalibre edilecek sabit bir ölçek faktörüdür — ampirik veri birikmeden önce makul bir varsayılan değerle başlanabilir, örn. 1.5-2.0 aralığı; ürün olgunlaştıkça ayarlanabilir.)

**C) Termal Verimlilik Puanı (%20)** — sadece throttling eşiğinin (38°C) altında kalan CC fazı verisiyle hesaplanır:

```
Puan_3 = 20 − (ΔT_CC_fazı × 2)
```

**Nihai Skor = Puan_1 + Puan_2 + Puan_3** (0'ın altına düşerse 0'a, 100'ün üstüne çıkarsa 100'e sabitlenir)

## 6. Zirve Watt Bilgilendirmesi (Skora Dahil Değil)

Kullanıcının merak edebileceği basit bir ek bilgi: test boyunca gözlemlenen en yüksek anlık watt değeri, puanlamadan bağımsız olarak ayrıca gösterilir.

- **Zirve Watt (`W_peak`):** Testin tamamı boyunca (CC fazıyla sınırlı kalmadan, CV fazı dahil, throttling filtresinden önceki ham veri dahil) gözlemlenen en yüksek anlık watt değeri. Bu ölçüm herhangi bir protokol/negotiate tespitine bağlı değildir — ham voltaj × akım ölçümüdür, dolayısıyla Kademe 1, 2 veya 3'ün hangisi tespit edilirse edilsin her testte hesaplanır ve gösterilir.
- Bu değer sadece bilgi amaçlıdır, herhangi bir yorum/karşılaştırma/etiketleme üretilmez ve **Nihai Skor'u hiçbir şekilde etkilemez** — skor zaten `W_ref` (negotiate edilen veya donanımsal gerçek tavan) üzerinden hesaplanıyor.

Ayrıca eğer `W_peak`, Kademe 1/2'de tespit edilen `W_ref`'i belirgin şekilde aşarsa (örn. adaptör kısa bir anlık spike ile negotiate edilen tavanın üstüne çıktıysa), bu bir ölçüm gürültüsü sinyali olarak ayrıca not düşülür — "Zirve değer, cihazın kabul ettiği anlaşma tavanının üzerinde ölçüldü; bu ölçüm gürültüsü olabilir" gibi.

## 7. Güven Etiketi

Aşağıdaki kurallar sırayla uygulanır, en düşük güven seviyesi geçerli olur:

| Koşul | Etiket |
|---|---|
| CC fazı süresi < 90 saniye | Düşük Güven — Tekrar Test Önerilir |
| Throttling nedeniyle verinin >%30'u filtrelendi | Orta Güven — Testin bir kısmı ısınma nedeniyle dışlandı |
| Kademe 3 (negotiate protokolü tespit edilemedi) | Orta/Düşük Güven — Referans donanım limiti varsayımına dayanıyor |
| Yukarıdakilerin hiçbiri yoksa | Yüksek Güven |

Birden fazla koşul aynı anda geçerliyse en düşük güven seviyesi gösterilir ve nedenleri ayrı ayrı listelenir.

## 8. Çıktı Formatı — Örnekler

**Modern cihaz (Kademe 1):**
```
🏆 Nihai Skor: 89/100 (Yüksek Güven)

Protokol: USB-PD 3.0 (PPS) — Negotiate Tavan: 45W
Test Koşulları: Pil %28→%38 | CC Fazı Süresi: 2dk 40sn

• Verimlilik: 41.2W / 45W (%91.5) → 45.8 / 50
• Kararlılık: CV = %2.1 (Çok Temiz Akım) → 27.9 / 30
• Termal: CC fazında +1.8°C artış → 16.4 / 20

Not: Throttling tespit edilmedi, tüm veri değerlendirmeye dahil edildi.

📊 Zirve Watt (bilgi amaçlı, skora dahil değil): 61.4W
```

**Eski cihaz (Kademe 3):**
```
🏆 Nihai Skor: 76/100 (Orta Güven)

Protokol: Tespit Edilemedi (Standart USB / Negotiate Yok)
Referans: Telefonun donanımsal giriş sınırı — 5V / 1.5A (7.5W)
Test Koşulları: Pil %31→%41 | CC Fazı Süresi: 3dk 05sn

• Verimlilik: 6.8W / 7.5W (%90.6) → 45.3 / 50
• Kararlılık: CV = %3.4 → 25.9 / 30
• Termal: CC fazında +1.2°C artış → 17.6 / 20

Not: Bu kombinasyonda dijital güç anlaşması (USB-PD/QC) tespit edilmedi.
Skor, telefonun donanım giriş limitine göre hesaplandı; bu nedenle güven
seviyesi bir kademe düşürüldü.

📊 Zirve Watt (bilgi amaçlı, skora dahil değil): 7.9W
```

## 9. Uygulama Notları (Android)

- Negotiate/protokol tespiti: `BatteryManager`, USB port yöneticisi (`UsbManager`), mümkünse root olmadan erişilebilen `POWER_SUPPLY_*` sysfs değerleri.
- Kademe 2 tespiti için üretici bazlı sinyaller (`POWER_SUPPLY_TYPE`, OEM broadcast'leri) cihazdan cihaza değişebilir — bilinmeyen/tanımlanamayan sinyaller doğrudan Kademe 3'e düşürülmeli, asla "muhtemelen QC'dir" gibi belirsiz bir varsayımla Kademe 2 olarak işaretlenmemeli.
- Donanım giriş limiti (Kademe 3 referansı) bulunamazsa (bazı eski cihazlarda sysfs değeri raporlanmayabilir), test "Referans Bulunamadı" olarak sonuçlandırılmalı ve skor hesaplanmamalı — anlamsız bir sayı üretmektense sonucu göstermemek tercih edilir.
