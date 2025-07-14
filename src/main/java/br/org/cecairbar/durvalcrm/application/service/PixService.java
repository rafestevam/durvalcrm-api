package br.org.cecairbar.durvalcrm.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;

@ApplicationScoped
public class PixService {
    
    public String gerarQRCode(BigDecimal valor, String identificador, String descricao) {
        // Por enquanto, retorna um QR code simulado
        // Futuramente integrar com API do banco
        return gerarQRCodeSimulado(valor, identificador, descricao);
    }
    
    private String gerarQRCodeSimulado(BigDecimal valor, String identificador, String descricao) {
        // Implementação temporária que gera um código PIX básico
        StringBuilder pixCode = new StringBuilder();
        
        // Payload Format Indicator
        pixCode.append("000201");
        
        // Point of Initiation Method
        pixCode.append("010212");
        
        // Merchant Account Information
        String chavePix = "contato@durvalcrm.org";
        String merchantInfo = String.format("0014BR.GOV.BCB.PIX01%02d%s", 
            chavePix.length(), chavePix);
        pixCode.append(String.format("26%02d%s", merchantInfo.length(), merchantInfo));
        
        // Merchant Category Code
        pixCode.append("52040000");
        
        // Transaction Currency (BRL)
        pixCode.append("5303986");
        
        // Transaction Amount
        String valorStr = valor.toString();
        pixCode.append(String.format("54%02d%s", valorStr.length(), valorStr));
        
        // Country Code
        pixCode.append("5802BR");
        
        // Merchant Name
        String merchantName = "Associacao";
        pixCode.append(String.format("59%02d%s", merchantName.length(), merchantName));
        
        // Merchant City
        String merchantCity = "Guarulhos";
        pixCode.append(String.format("60%02d%s", merchantCity.length(), merchantCity));
        
        // Additional Data Field Template
        String additionalData = String.format("05%02d%s", identificador.length(), identificador);
        pixCode.append(String.format("62%02d%s", additionalData.length(), additionalData));
        
        // CRC16 (será calculado)
        pixCode.append("6304");
        
        // Calcular e adicionar CRC
        String crc = calcularCRC16(pixCode.toString());
        pixCode.append(crc);
        
        return pixCode.toString();
    }
    
    private String calcularCRC16(String payload) {
        // Implementação simplificada do CRC16
        // Em produção, usar biblioteca específica
        return "ABCD"; // Placeholder
    }
}