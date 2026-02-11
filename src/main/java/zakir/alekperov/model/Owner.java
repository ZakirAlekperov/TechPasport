package zakir.alekperov.model;

import javafx.beans.property.*;

/**
 * Модель данных для правообладателя
 */
public class Owner {
    
    private final StringProperty fullName;          // ФИО / Наименование организации
    private final StringProperty ownerType;         // Тип: физическое лицо / юридическое лицо
    private final StringProperty documentType;      // Тип документа (свидетельство, договор и т.д.)
    private final StringProperty documentNumber;    // Номер документа
    private final StringProperty documentDate;      // Дата документа
    private final StringProperty registrationNumber; // Номер государственной регистрации
    private final StringProperty registrationDate;  // Дата государственной регистрации
    private final DoubleProperty shareSize;         // Доля в праве (например, 1/2, 1/3, или 1.0 для 100%)
    
    public Owner() {
        this.fullName = new SimpleStringProperty("");
        this.ownerType = new SimpleStringProperty("Физическое лицо");
        this.documentType = new SimpleStringProperty("");
        this.documentNumber = new SimpleStringProperty("");
        this.documentDate = new SimpleStringProperty("");
        this.registrationNumber = new SimpleStringProperty("");
        this.registrationDate = new SimpleStringProperty("");
        this.shareSize = new SimpleDoubleProperty(1.0); // По умолчанию 100%
    }
    
    public Owner(String fullName, String ownerType, String documentType, String documentNumber,
                String documentDate, String registrationNumber, String registrationDate, double shareSize) {
        this.fullName = new SimpleStringProperty(fullName);
        this.ownerType = new SimpleStringProperty(ownerType);
        this.documentType = new SimpleStringProperty(documentType);
        this.documentNumber = new SimpleStringProperty(documentNumber);
        this.documentDate = new SimpleStringProperty(documentDate);
        this.registrationNumber = new SimpleStringProperty(registrationNumber);
        this.registrationDate = new SimpleStringProperty(registrationDate);
        this.shareSize = new SimpleDoubleProperty(shareSize);
    }
    
    // Full Name (ФИО)
    public String getFullName() { return fullName.get(); }
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public StringProperty fullNameProperty() { return fullName; }
    
    // Owner Type (Тип правообладателя)
    public String getOwnerType() { return ownerType.get(); }
    public void setOwnerType(String ownerType) { this.ownerType.set(ownerType); }
    public StringProperty ownerTypeProperty() { return ownerType; }
    
    // Document Type (Тип документа)
    public String getDocumentType() { return documentType.get(); }
    public void setDocumentType(String documentType) { this.documentType.set(documentType); }
    public StringProperty documentTypeProperty() { return documentType; }
    
    // Document Number (Номер документа)
    public String getDocumentNumber() { return documentNumber.get(); }
    public void setDocumentNumber(String documentNumber) { this.documentNumber.set(documentNumber); }
    public StringProperty documentNumberProperty() { return documentNumber; }
    
    // Document Date (Дата документа)
    public String getDocumentDate() { return documentDate.get(); }
    public void setDocumentDate(String documentDate) { this.documentDate.set(documentDate); }
    public StringProperty documentDateProperty() { return documentDate; }
    
    // Registration Number (Номер регистрации)
    public String getRegistrationNumber() { return registrationNumber.get(); }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber.set(registrationNumber); }
    public StringProperty registrationNumberProperty() { return registrationNumber; }
    
    // Registration Date (Дата регистрации)
    public String getRegistrationDate() { return registrationDate.get(); }
    public void setRegistrationDate(String registrationDate) { this.registrationDate.set(registrationDate); }
    public StringProperty registrationDateProperty() { return registrationDate; }
    
    // Share Size (Доля в праве)
    public double getShareSize() { return shareSize.get(); }
    public void setShareSize(double shareSize) { this.shareSize.set(shareSize); }
    public DoubleProperty shareSizeProperty() { return shareSize; }
    
    /**
     * Форматированная доля (например, "1/2", "1/3", "1")
     */
    public String getFormattedShare() {
        double share = shareSize.get();
        if (share == 1.0) {
            return "1";
        } else if (share == 0.5) {
            return "1/2";
        } else if (share == 0.333 || share == 0.3333) {
            return "1/3";
        } else if (share == 0.25) {
            return "1/4";
        } else {
            return String.format("%.4f", share);
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s), доля: %s. Документ: %s №%s от %s. Регистрация: №%s от %s",
            fullName.get(), ownerType.get(), getFormattedShare(),
            documentType.get(), documentNumber.get(), documentDate.get(),
            registrationNumber.get(), registrationDate.get());
    }
}