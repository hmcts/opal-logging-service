module "servicebus-namespace" {
  source                  = "git@github.com:hmcts/terraform-module-servicebus-namespace?ref=4.x"
  name                    = "${var.product}-${var.component}"
  resource_group_name     = azurerm_resource_group.shared_resource_group.name
  location                = var.location
  env                     = var.env
  common_tags             = var.common_tags
  sku                     = var.service_bus_sku
  enable_private_endpoint = var.servicebus_enable_private_endpoint
}


module "servicebus-queue-logging-pdpl" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-queue?ref=master"
  name                  = "logging-pdpl"
  namespace_name        = module.servicebus-namespace.name
  resource_group_name   = azurerm_resource_group.rg.name
  depends_on = [module.servicebus-namespace]
}


output "sb_primary_send_and_listen_connection_string" {
  value     = module.servicebus-namespace.primary_send_and_listen_connection_string
  sensitive = true
}

output "sb_primary_send_and_listen_shared_access_key" {
  value     = module.servicebus-namespace.primary_send_and_listen_shared_access_key
  sensitive = true
}

resource "azurerm_key_vault_secret" "servicebus_primary_connection_string" {
  name         = "hmc-servicebus-connection-string"
  value        = module.servicebus-namespace.primary_send_and_listen_connection_string
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

resource "azurerm_key_vault_secret" "servicebus_primary_shared_access_key" {
  name         = "hmc-servicebus-shared-access-key"
  value        = module.servicebus-namespace.primary_send_and_listen_shared_access_key
  key_vault_id = data.azurerm_key_vault.key_vault.id
}