<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html <#if realm.internationalizationEnabled> lang="${locale.currentLanguageTag}"</#if>>

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
    
    <!-- Tailwind CSS para consistência com o DurvalCRM -->
    <script src="https://cdn.tailwindcss.com"></script>
    
    <!-- Meta viewport para responsividade -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body class="bg-white min-h-screen">
  <div class="min-h-screen flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <!-- Header com logo DurvalCRM -->
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <div class="flex justify-center">
        <img class="w-25 h-25" style="width: 100px; height: 100px;" src="${url.resourcesPath}/img/logo-durvalcrm.svg" alt="DurvalCRM" />
      </div>
      <h2 class="mt-6 text-center text-4xl font-bold tracking-tight text-blue-600">
        DurvalCRM
      </h2>
      <#if !(auth?has_content && auth.showUsername() && !auth.showResetCredentials())>
        <#if displayRequiredFields>
          <div class="mt-2 text-center text-sm text-gray-600">
            <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
          </div>
        </#if>
      </#if>
    </div>

    <!-- Conteúdo principal -->
    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        
        <!-- Mensagens de erro/sucesso -->
        <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
          <div class="alert-${message.type} ${properties.kcAlertClass!} mb-4 p-4 rounded-md border-l-4 
                      <#if message.type = 'success'>border-green-400 bg-green-50</#if>
                      <#if message.type = 'warning'>border-yellow-400 bg-yellow-50</#if>
                      <#if message.type = 'error'>border-red-400 bg-red-50</#if>
                      <#if message.type = 'info'>border-blue-400 bg-blue-50</#if>">
            <div class="flex">
              <div class="flex-shrink-0">
                <!-- Ícone baseado no tipo de mensagem -->
                <#if message.type = 'success'>
                  <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                  </svg>
                <#elseif message.type = 'error'>
                  <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
                  </svg>
                <#elseif message.type = 'warning'>
                  <svg class="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                  </svg>
                <#else>
                  <svg class="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
                  </svg>
                </#if>
              </div>
              <div class="ml-3">
                <p class="text-sm 
                         <#if message.type = 'success'>text-green-800</#if>
                         <#if message.type = 'warning'>text-yellow-800</#if>
                         <#if message.type = 'error'>text-red-800</#if>
                         <#if message.type = 'info'>text-blue-800</#if>">
                  <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
                </p>
              </div>
            </div>
          </div>
        </#if>

        <!-- Conteúdo do formulário -->
        <div class="space-y-6">
          <#nested "form">
        </div>

        <!-- Informações adicionais -->
        <#if displayInfo>
          <div class="mt-6 text-center">
            <#nested "info">
          </div>
        </#if>
      </div>
    </div>

    <!-- Footer -->
    <div class="mt-8 text-center">
      <p class="text-xs text-gray-500">
        &copy; ${.now?string("yyyy")} DurvalCRM. Todos os direitos reservados.
      </p>
    </div>
  </div>
</body>
</html>
</#macro>