#!/usr/bin/env bash
# Espera a que la API desplegada responda con la versión esperada y deja el resultado en el resumen
# del workflow, con la duración desde el inicio (evidencia de ESC-07 y ESC-15).
# Uso: verificar-despliegue.sh <grupo-de-recursos> <aplicacion> <version-esperada> <inicio-en-segundos>
set -euo pipefail

readonly GRUPO="$1"
readonly APP="$2"
readonly VERSION_ESPERADA="$3"
readonly INICIO="$4"
readonly INTENTOS=30
readonly ESPERA_SEGUNDOS=10

fqdn=$(az containerapp show --resource-group "$GRUPO" --name "$APP" \
  --query properties.configuration.ingress.fqdn --output tsv)
readonly URL="https://$fqdn"

for ((intento = 1; intento <= INTENTOS; intento++)); do
  version=$(curl -fsS --max-time 10 "$URL/actuator/info" 2>/dev/null | jq -r '.app.version // empty' || true)
  if [[ "$version" == "$VERSION_ESPERADA" ]]; then
    estado=$(curl -fsS --max-time 10 "$URL/actuator/health" | jq -r '.status')
    segundos=$(( $(date +%s) - INICIO ))
    {
      echo "### Despliegue verificado"
      echo ""
      echo "| Dato | Valor |"
      echo "|---|---|"
      echo "| URL | $URL |"
      echo "| Versión | \`$version\` |"
      echo "| Salud | $estado |"
      echo "| Duración desde el inicio | $((segundos / 60)) min $((segundos % 60)) s |"
    } >> "$GITHUB_STEP_SUMMARY"
    echo "La API responde con la versión $version en $URL, $segundos s después del inicio"
    exit 0
  fi
  echo "Intento $intento de $INTENTOS: la API todavía responde «${version:-sin respuesta}»"
  sleep "$ESPERA_SEGUNDOS"
done

echo "::error::La API no respondió con la versión $VERSION_ESPERADA después de $((INTENTOS * ESPERA_SEGUNDOS)) s"
exit 1
