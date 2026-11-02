# Fit — V2 ve V3 Yol Haritası

**Son güncelleme:** 3 Kasım 2026
**Ürün:** Android için kişisel kilo artışı ve gelişim takibi
**Mevcut temel:** V1, Jetpack Compose, cihaz üzerinde yerel kayıt

## Ürün yönü

Fit, kullanıcının günlük kalori ve makrolarını manuel kaydettiği; kilo, su, antrenman ve fotoğraf ilerlemesini takip ettiği kişisel bir araçtır. Uygulama kayıtları anlaşılır biçimde göstermeli, kullanıcı adına yiyecek seçmemeli ve kaçırılan kalorileri cezalandırıcı telafi hedeflerine dönüştürmemelidir.

### Sabit ürün kararları

- Ana hedef: **73,0 kg**. Öncelikli dönem **90 gün**; bu süre hedefe ulaşma garantisi değildir ve takip gerektiğinde devam eder.
- Kullanıcı öğünlerini ve makrolarını manuel girer. Yiyecek önerisi yoktur.
- Eksik günlük kayıt sıfır tüketim kabul edilmez; kullanıcı günü tamamlandı olarak işaretler.
- Kalori fazlası yalnızca aynı haftada, kalan günlere en çok **100 kcal/gün** azaltım olarak yansıtılır. Eksik kalori borç olarak sonraki güne taşınmaz.
- İlk sürümde barkod tarama yoktur. V2/V3'te ancak güvenilir ürün verisi ve uygun lisans bulunursa değerlendirilir.
- Koyu ve açık tema desteklenir. Kayıtlar kullanıcı kontrolünde kalır.

### V1'de bulunanlar

- Günlük manuel öğün/makro kaydı; geçmiş plan gününe gitme ve öğün düzenleme/silme.
- Su ve antrenman günü işareti, tartı kaydı/grafiği, ilerleme fotoğrafları.
- 3.000 kcal başlangıç hedefi ve koşullu hedef güncelleme/haftalık dengeleme mantığı.
- Açık/koyu tema ve profil ekranı.

V1 hedefleri başlangıç değeridir; kullanıcı için kesin veya değişmez reçete olarak sunulmamalıdır. V1 verileri cihazda saklanır. Mevcut proje Android debug APK üretir.

## V2 — Kontrol, güven ve daha iyi takip

V2'nin amacı yeni özellik sayısını artırmaktan önce kullanıcının hedefini, kayıtlarını ve uygulamanın hesaplarını yönetilebilir hale getirmektir.

### V2.0 — Profil ve veri güvenilirliği

1. **Düzenlenebilir profil:** doğum tarihi, boy, başlangıç kilosu, aktivite düzeyi, antrenman sıklığı, hedef kilo ve plan başlangıç tarihi.
2. **Hedef kontrolü:** kalori ve makro hedeflerini elle değiştirme veya uygulamanın hesapladığı öneriyi kabul etme. Hedefin kaynağı, geçerlilik tarihi ve değişim nedeni gösterilir.
3. **Plan günü kuralı:** 90 günlük öncelikli dönem ile kullanıcının istediği bir günlük ek süreyi tek ve açık formülle göstermek. Gün 1, gün 90 ve ek günün takvim karşılıkları uygulama içinde tutarlı olmalı.
4. **Tarih düzeltme:** tartı kayıtlarına tarih verme; geçmiş kilo, su ve antrenman kayıtlarını düzenleme/silme. Bir güne birden fazla tartı girildiğinde ortalama ve son ölçümün nasıl kullanıldığı açıklanır.
5. **Yedekleme ve taşıma:** kayıtları JSON/CSV olarak dışa aktarma ve geri alma. Uygulama güncellenirken eski kayıtlar korunur.
6. **Dengeleme açıklaması:** bugünkü hedefin normal hedef mi, haftalık fazla alımdan sonra azaltılmış hedef mi olduğu ve hesaplamanın hangi günlerden geldiği görünür.
7. **Arayüz düzeltmeleri:** takvim, tarih ve sayısal alanların Türkçe gösterimi; erişilebilir yazı boyutu; boş, yükleniyor ve hata durumlarının tamamlanması.

**V2.0 tamamlanma ölçütleri:** Profil ve plan ayarları uygulamayı yeniden açınca korunur; geçmiş kayıtlar doğru tarihte düzenlenir; yedek dışa aktarılıp geri alınabilir; gün eksikliği veya fazla alım hedefi sessizce ve açıklamasız değiştirmez.

### V2.1 — İlerleme ve antrenman

- Günlük tartı noktalarıyla birlikte 7 günlük ortalamayı ve hedef eğilimini ayrı çizgilerle gösterme.
- Bel, göğüs ve kol gibi isteğe bağlı ölçüler; tarihli fotoğraf albümü ve kısa notlar.
- Antrenman kaydını isteğe bağlı olarak egzersiz, set, tekrar ve kullanılan ağırlıkla genişletme.
- Kilo eğilimiyle kuvvet gelişimini aynı dönem içinde karşılaştırma; kişiye program dayatmama.

**V2.1 tamamlanma ölçütleri:** Kullanıcı tartı dalgalanmasını uzun dönem eğiliminden ayırabilir; antrenman gününü ister basitçe işaretler, ister ayrıntılı kaydeder; fotoğraf ve ölçüler tarihleriyle bulunabilir.

### V2.2 — Daha hızlı manuel besin girişi

- Sık kullanılan öğünleri ve kendi oluşturduğu ürünleri şablon olarak kaydetme.
- SSN Gainer ve HIQ Creatine gibi ürünleri kullanıcı etiketindeki porsiyon ve değerlerle tanımlama.
- Barkod tarama için kaynak araştırması: veri kapsamı, Türkiye ürün eşleşme oranı, lisans ve ürün etiketiyle doğrulama. Bu koşullar sağlanmazsa barkodu erteleme.
- Hiçbir ürün veya takviyenin kullanıcı onayı olmadan kalori/makro toplamına eklenmemesi.

## V3 — Kişisel gelişim paneli

V3'ün amacı beslenme, kilo eğilimi ve antrenman verisini tek yerde birleştirerek kullanıcının **ne olduğunu ve uygulamanın neden böyle gösterdiğini** anlamasını sağlamaktır. V3, otomatik yiyecek önerisi veya zorunlu antrenman reçetesi sunmaz.

### V3.1 — Sağlam veri altyapısı ve isteğe bağlı eşitleme

- Yerel veriyi yapılandırılmış veritabanına taşıma; sürüm yükseltmelerinde şema geçişlerini ve eski kayıtların korunmasını planlama.
- Çevrimdışı tam kullanım; internet yokken öğün, kilo ve antrenman girişi devam etmeli.
- İsteğe bağlı, açık rızaya dayalı yedekleme/eşitleme. Hesap açmadan yerel kullanım korunmalı.
- Çakışma çözümü: aynı kayıt iki cihazda değiştiğinde kullanıcıya hangi sürümü tutacağını gösterme; sessiz veri kaybı olmaması.
- Veriyi dışa aktarma, içe aktarma ve tüm kişisel verileri silme seçenekleri.

### V3.2 — Açıklanabilir gelişim analizi

- Haftalık rapor: kalori/makro ortalamaları, tamamlanmış gün sayısı, kilo ortalaması ve antrenman sıklığı.
- Planlanan hedef ile gerçekleşen eğilimi yan yana gösterme; değişimin hangi kayıt dönemine dayandığını açıklama.
- Veri yetersizse sonuç üretmek yerine "henüz yeterli kayıt yok" demek.
- Kilo değişimini tek tartı ölçümüne göre yorumlamama; kreatin başlangıcı, hastalık veya sıvı değişimi gibi işaretleri analizde görünür tutma.
- Hedef değişikliklerini önizleme ve kullanıcıya onaylatma; geçmiş hedefleri ve nedenlerini saklama.
- İsteğe bağlı senaryo görünümü: mevcut eğilim sürerse olası tarih aralığı. Kesin tarih veya sonuç vaadi vermeme.

### V3.3 — Beslenme ve ürün kütüphanesi

- Kullanıcının kendi besin/ürün kütüphanesi, porsiyonları ve kaydedilmiş öğünleri.
- Etiketten manuel ürün oluşturma ve ürün bilgisinin kaynağını/tarihini saklama.
- Barkod yalnızca ürün eşleşmesi güvenilir ve kullanım koşulları uygun olduğunda. Bulunamayan ürün manuel kayda kolay dönmeli.
- Kayıt arama, kopyalama ve geçmiş öğünü yeni güne ekleme.
- Ürün verisi değişirse geçmiş kayıtları sessizce yeniden hesaplamama; eski kayıt, kullanılan değerlerle korunmalı.

### V3.4 — Birleşik gelişim görünümü

- Kilo, 7 günlük ortalama, bel ölçüsü, fotoğraf ve antrenman kuvvet göstergelerini ortak zaman çizelgesinde gösterme.
- Fotoğrafları özel tutma; paylaşım varsayılan olarak kapalı ve her görseli dışa aktarma/silme olanağı.
- Kişisel rekorlar ve antrenman devamlılığı gibi nötr özetler; suçlayıcı seri/streak tasarımı kullanmama.
- Aylık ve 90 günlük raporu cihazda oluşturma; kullanıcı isterse PDF/CSV dışa aktarma.

### V3.5 — Hatırlatmalar ve entegrasyonlar

- Öğün, su, tartı veya antrenman hatırlatmaları kullanıcı tarafından ayrı ayrı açılıp kapatılmalı; sessiz saatler desteklenmeli.
- Ana ekran widget'ı: bugünkü hedef ve kayıt durumu; hassas kilo/fotoğraf bilgisi varsayılan olarak gösterilmemeli.
- Sağlık platformlarıyla entegrasyon (ör. Android Health Connect) ancak mevcut izinleri, veri eşlemesini ve desteklenen alanları doğruladıktan sonra; içe alınan kayıt kaynağı işaretlenmeli.

### V3.6 — Yayına hazırlık

- İmzalı release APK/AAB ve sürüm numaralandırma.
- Gizlilik açıklaması, veri dışa/silme akışı, izinlerin gerekçesi ve yedekleme davranışı.
- Desteklenen Android sürümlerinde kurulum, yükseltme ve veri korunumu kontrolü.
- Erişilebilirlik, koyu/açık tema, Türkçe metinler, boş ekranlar ve çevrimdışı durumların son kontrolü.

## Yol haritası ve geçiş kapıları

| Aşama | Odak | Sonraki aşamaya geçiş ölçütü |
|---|---|---|
| V2.0 | Profil/hedef kontrolü, tarihli kayıt düzenleme, yedekleme, hesap açıklamaları | Kayıtlar düzenlenebiliyor ve geri yüklenebiliyor; plan günü ve dengeleme kuralları ürün içinde anlaşılır |
| V2.1 | Tartı ortalamaları, ölçüler/fotoğraflar, detaylı antrenman | Kilo ve kuvvet gelişimi aynı zaman aralığında görülebiliyor |
| V2.2 | Öğün/ürün şablonları, etiket bilgisi, barkod uygunluk kararı | Manuel giriş hızlı; ürün verisi güvenilir ve kaynağı belli |
| V3.1 | Veritabanı, çevrimdışı kullanım, isteğe bağlı yedek/eşitleme | Veri taşınması ve çakışmalar kullanıcı kontrolünde; veri kaybı yok |
| V3.2–3.4 | Açıklanabilir analiz ve birleşik gelişim paneli | Her içgörü dayandığı dönemi gösteriyor; yetersiz veride sonuç üretmiyor |
| V3.5–3.6 | Hatırlatma, entegrasyon ve yayın hazırlığı | İzinler açık rızalı; release imzalı; gizlilik ve veri silme akışı hazır |

## V2/V3 başlamadan netleştirilecek kararlar

1. **90 gün + 1 gün:** 90 günün başlangıç günü dahil mi; ek gün 91. gün mü, yoksa hedef tarih için tolerans günü mü? Uygulama ve rapor aynı sayım kuralını kullanmalı.
2. **73,0 kg teyidi:** hedef tek ölçümle mi, yoksa kullanıcının mevcut 7 günlük ortalama kuralıyla mı tamamlanmış sayılacak? V1 şu anda ortalama ve takip teyidi yaklaşımını kullanıyor.
3. **Hedef düzenleme:** kullanıcı elle kalori/makro değiştirdiğinde otomatik güncelleme bu değeri koruyacak mı, yoksa kullanıcıya her seferinde onay mı soracak?
4. **Eşitleme tercihi:** V3'te bulut eşitleme hesap açma zorunluluğu olmadan ve açık tercihle sunulmalı mı? Öneri: evet, tamamen isteğe bağlı.
5. **Barkod kararı:** ürün verisi kaynağı ve lisans koşulları araştırılmadan özellik takvime alınmamalı.

## Kapsam dışında (şimdilik)

- Yiyecek öneren veya kullanıcının yerine öğün planlayan AI.
- Eksik kaloriyi sonraki günlere borç yazan agresif telafi sistemi.
- Kullanıcı onayı olmadan hedef veya geçmiş kayıt değiştirme.
- Güvenilir ürün eşleşmesi olmayan barkod kataloğu.
- Sosyal akış, herkese açık kilo/fotoğraf paylaşımı veya zorunlu hesap.
