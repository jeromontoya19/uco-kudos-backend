#!/usr/bin/env bash
# Despliega una imagen en Azure Container Apps. Lo usan el CD y la vuelta atrás.
# Uso: desplegar-imagen.sh <grupo-de-recursos> <aplicacion> <imagen:etiqueta>
set -euo pipefail

readonly GRUPO="$1"
readonly APP="$2"
readonly IMAGEN="$3"
# La API escucha en el 8080 (application.yml); la aplicación se creó con una imagen de prueba en el 80.
readonly PUERTO_API=8080

az extension add --name containerapp --upgrade --allow-preview true --only-show-errors

echo "Desplegando $IMAGEN en $APP"
az containerapp update --resource-group "$GRUPO" --name "$APP" --image "$IMAGEN" --only-show-errors --output none
az containerapp ingress update --resource-group "$GRUPO" --name "$APP" --target-port "$PUERTO_API" \
  --only-show-errors --output none
