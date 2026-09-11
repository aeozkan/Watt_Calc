# WattBench — Bilinen Hatalar ve Takip Listesi (Known Bugs & Issues)

Bu doküman, kapalı test (Closed Test) sürecinde bilerek bekletilen veya kullanıcı geri bildirimlerine göre düzeltilecek olan teknik bulguları içerir.

---

### BUG-001: Home Tuşu Sonrası Son Uygulamalardan Kaydırmada ViewModel Belleğinin Korunması (Kaldığı Yerden Devam Etme)

- **Durum:** Beklemede (Kullanıcı geri bildirimine bırakıldı — Düzeltilmeyecek)
- **Tarih:** 11 Eylül 2026
- **Kategori:** Uygulama Yaşam Döngüsü (Activity / ViewModel Lifecycle & Process State)

#### 📋 Belirti ve Senaryo:
1. **Senaryo 1 (Doğru Çalışan - Tam Kapanma):**
   - Uygulama ekranda açıkken doğrudan **Son Uygulamalar (Recent Apps / Overview)** tuşuna basılıp kart yukarı/yana kaydırılarak kapatılırsa:
   - Uygulama süreci tamamen sonlandırılır (`Process Death`), tekrar açıldığında sıfırdan ve temiz başlar.

2. **Senaryo 2 (Bug Durumu - Kaldığı Yerden Devam Etme):**
   - Uygulama açıkken önce navigasyon çubuğundaki **Home tuşuna** basılır (Uygulama arka plana / `onStop` durumuna geçer).
   - Ardından **Son Uygulamalar** tuşuna basılıp WattBench kartı ekrandan kaydırılarak kapatılır.
   - Bildirim çubuğundaki canlı bildirim başarıyla kaybolur (`PowerTelemetryService.onTaskRemoved` çalışır).
   - **Sorun:** Uygulama simgesine dokunulup tekrar açıldığında, uygulama sıfırdan başlamak yerine grafiğin ve sayaçların kaldığı yerden devam ettiği görülür.

#### 🔍 Teknik Neden:
- Kullanıcı doğrudan son uygulamalardan kaydırdığında Android OS ana Activity görev yığınını (`TaskStack`) yok eder.
- Ancak önce `Home` tuşuna basıldığında Android aktiviteyi önbelleğe (`Cached / Background Process`) alır. Görev kaydırıldığında Servis'e `onTaskRemoved` sinyali gidip servis dursa da, OS işletim sistemi bellek baskısı görmediği için Activity'nin ViewModel belleğini (`ViewModelStore`) tamamen sıfırlamayabilir. Uygulama tekrar açıldığında Activity sıfırdan oluşmak yerine önbellekten canlandırılır (`Re-creation from cached state`).

#### 🛠️ Planlanan Çözüm (İleride Yapılacak):
- Servis'in `onTaskRemoved()` metodunda veya `MainActivity`'nin yaşam döngüsünde görev kaldırıldığında süreci temiz sonlandırmak için:
  1. `PowerTelemetryService.resetLiveHistory()` çağrısının yapılması,
  2. Veya `onTaskRemoved` içinde `Process.killProcess(Process.myPid())` / `exitProcess(0)` ile sürecin işletim sistemi seviyesinde temiz bir şekilde kapatılması.
