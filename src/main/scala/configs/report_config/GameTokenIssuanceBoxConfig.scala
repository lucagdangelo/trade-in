package configs.report_config

case class GameTokenIssuanceBoxConfig(
                                     var gameTokenIssuanceContract: String,
                                     var gameTokenId: String,
                                     var boxId: String,
                                     var txId: String
                                     )
