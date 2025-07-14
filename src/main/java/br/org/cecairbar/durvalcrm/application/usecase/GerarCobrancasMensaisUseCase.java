package br.org.cecairbar.durvalcrm.application.usecase;

import br.org.cecairbar.durvalcrm.domain.model.Mensalidade;
import br.org.cecairbar.durvalcrm.domain.repository.AssociadoRepository;
import br.org.cecairbar.durvalcrm.domain.repository.MensalidadeRepository;
import br.org.cecairbar.durvalcrm.application.dto.ResultadoGeracaoDTO;
import br.org.cecairbar.durvalcrm.application.service.PixService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class GerarCobrancasMensaisUseCase {

    @Inject
    AssociadoRepository associadoRepository;

    @Inject
    MensalidadeRepository mensalidadeRepository;

    @Inject
    PixService pixService;

    // Valor fixo da mensalidade: R$ 10,00 + R$ 0,90 taxa = R$ 10,90
    private static final BigDecimal VALOR_MENSALIDADE = new BigDecimal("10.90");

    @Transactional
    public ResultadoGeracaoDTO executar(int mes, int ano) {
        // Buscar todos os associados ativos
        var associadosAtivos = associadoRepository.findByAtivo(true);
        
        int geradas = 0;
        int jaExistiam = 0;

        for (var associado : associadosAtivos) {
            // Verificar se já existe mensalidade para este período
            if (mensalidadeRepository.existsByAssociadoEPeriodo(associado.getId(), mes, ano)) {
                jaExistiam++;
                continue;
            }

            // Criar nova mensalidade
            Mensalidade mensalidade = Mensalidade.criar(
                associado.getId(), 
                mes, 
                ano, 
                VALOR_MENSALIDADE
            );

            // Gerar QR Code PIX
            String qrCode = pixService.gerarQRCode(
                VALOR_MENSALIDADE,
                mensalidade.getIdentificadorPix(),
                String.format("Mensalidade %02d/%d - %s", mes, ano, associado.getNomeCompleto())
            );
            mensalidade.setQrCodePix(qrCode);

            // Salvar mensalidade
            mensalidadeRepository.save(mensalidade);
            geradas++;
        }

        return new ResultadoGeracaoDTO(geradas, jaExistiam, associadosAtivos.size());
    }
}