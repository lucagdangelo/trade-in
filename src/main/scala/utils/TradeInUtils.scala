package utils

import builders.contract_builders.{CardValueMappingContractBuilder, CardValueMappingIssuanceContractBuilder, GameLPContractBuilder, GameLPIssuanceContractBuilder, GameTokenIssuanceContractBuilder, PlayerProxyContractBuilder}
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.{Address, BlockchainContext, ErgoContract}

import java.nio.file.{Files, Paths}
import java.time.{LocalDateTime, ZoneId}
import scala.util.Try

object TradeInUtils {

  // CLI logo
  final val tradeInLogo: String =
    """
      |
      |'########'########::::'###:::'########:'########::::::::'####'##::: ##:
      |... ##..::##.... ##::'## ##:::##.... ##:##.....:::::::::. ##::###:: ##:
      |::: ##::::##:::: ##:'##:. ##::##:::: ##:##::::::::::::::: ##::####: ##:
      |::: ##::::########:'##:::. ##:##:::: ##:######::'#######: ##::## ## ##:
      |::: ##::::##.. ##:::#########:##:::: ##:##...:::........: ##::##. ####:
      |::: ##::::##::. ##::##.... ##:##:::: ##:##::::::::::::::: ##::##:. ###:
      |::: ##::::##:::. ##:##:::: ##:########::########::::::::'####:##::. ##:
      |:::..::::..:::::..:..:::::..:........::........:::::::::....:..::::..::
      |
      |""".stripMargin

  // File Paths
  final val TRADEIN_SETUP_CONFIG_FILE_PATH: String = "settings/tradein_setup_config.json"
  final val TRADEIN_REPORT_CONFIG_FILE_PATH: String = "settings/tradein_report_config.json"
  final val TRADEIN_GAME_TOKEN_IMG_DIRECTORY_PATH: String = "img/"

  // ErgoScript contracts
  final val GAME_TOKEN_ISSUANCE_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/game_token_issuance/v1/ergoscript/game_token_issuance.es")).stripMargin
  final val GAME_LP_ISSUANCE_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/game_lp_issuance/v1/ergoscript/game_lp_issuance.es")).stripMargin
  final val GAME_LP_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/game_lp/v1/ergoscript/game_lp.es")).stripMargin
  final val CARD_VALUE_MAPPING_ISSUANCE_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/card_value_mapping_issuance/v1/ergoscript/card_value_mapping_issuance.es")).stripMargin
  final val CARD_VALUE_MAPPING_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/card_value_mapping/v1/ergoscript/card_value_mapping.es")).stripMargin
  final val PLAYER_PROXY_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/player_proxy/v1/ergoscript/player_proxy.es")).stripMargin

  // Box Values
  final val SAFE_STORAGE_RENT_VALUE: Long = 1000000000L
  final val MIN_BOX_VALUE: Long = 1000000L

  // Network Info
  final val ERGO_EXPLORER_TX_URL_PREFIX_MAINNET: String = "https://explorer.ergoplatform.com/en/transactions/"
  final val ERGO_EXPLORER_TX_URL_PREFIX_TESTNET: String = "https://testnet.ergoplatform.com/en/transactions/"

  // Node Info
  final val DEFAULT_LOCAL_NODE_MAINNET_API_URL: String = "http://127.0.0.1:9053/"
  final val DEFAULT_LOCAL_NODE_TESTNET_API_URL: String = "http://127.0.0.1:9052/"

  /**
   * Get a time-zone timestamp.
   *
   * @param zone The desired timezone.
   * @return A time-zone timestamp, with date and time.
   */
  def getTimeStamp(zone: String): String = {

    // Get the date and time in UTC format
    val dateTime: LocalDateTime = LocalDateTime.now(ZoneId.of(zone))

    // Format the time string
    val date: String = dateTime.toString.split("[T]")(0)
    val time: String = dateTime.toString.split("[T]")(1).split("\\.")(0)
    val timestamp: String = s"[$zone $date $time]"
    timestamp

  }

  /**
   * Check if the api url is from the local node, either for MAINNET or TESTNET
   *
   * @param apiUrl Node API url.
   * @return The boolean value if the api url matched the local node url
   */
  def isLocalNodeApiUrl(apiUrl: String): Boolean = apiUrl.equals(DEFAULT_LOCAL_NODE_MAINNET_API_URL) || apiUrl.equals(DEFAULT_LOCAL_NODE_TESTNET_API_URL)

  def compileContracts(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Unit = {

    // compile player proxy contract
    val proxyResult = compilePlayerProxy
    if (proxyResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: PLAYER PROXY CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: PLAYER PROXY CONTRACT ==========" + Console.RESET)
    }

    // compile card value mapping contract
    val cardValueMappingResult = compileCardValueMapping
    if (cardValueMappingResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: CARD VALUE MAPPING CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: CARD VALUE MAPPING CONTRACT ==========" + Console.RESET)
    }

    // compile card value mapping issuance contract
    val cardValueMappingIssuanceResult = compileCardValueMappingIssuance
    if (cardValueMappingIssuanceResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: CARD VALUE MAPPING ISSUANCE CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: CARD VALUE MAPPING ISSUANCE CONTRACT ==========" + Console.RESET)
    }

    // compile game lp contract
    val lpResult = compileGameLP
    if (lpResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: GAME LP CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: GAME LP CONTRACT ==========" + Console.RESET)
      println(lpResult.get)
    }

    // compile game lp issuance contract
    val lpIssuanceResult = compileGameLPIssuance
    if (lpIssuanceResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: GAME LP ISSUANCE CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: GAME LP ISSUANCE CONTRACT ==========" + Console.RESET)
      println(lpResult.get)
    }

    // compile game token issuance contract
    val gameTokenIssuanceResult = compileGameTokenIssuance
    if (gameTokenIssuanceResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: GAME TOKEN ISSUANCE CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: GAME TOKEN ISSUANCE CONTRACT ==========" + Console.RESET)
      println(lpResult.get)
    }

  }

  def compileGameTokenIssuance(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: GAME TOKEN ISSUANCE CONTRACT ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = GameTokenIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.gameTokenIssuanceBox.gameTokenIssuanceContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compileGameLPIssuance(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: GAME LP ISSUANCE CONTRACT ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = GameLPIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.gameLPIssuanceBox.gameLPIssuanceContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compileGameLP(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: GAME LP CONTRACT ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = GameLPContractBuilder(setupConfig, reportConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.gameLPBox.gameLPContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compileCardValueMappingIssuance(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: CARD VALUE MAPPING ISSUANCE CONTRACT ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = CardValueMappingIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.cardValueMappingIssuanceBox.cardValueMappingIssuanceContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compileCardValueMapping(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: CARD VALUE MAPPING CONTRACT ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = CardValueMappingContractBuilder().toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.cardValueMappingBox.cardValueMappingContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compilePlayerProxy(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: PLAYER PROXY CONTRACT ==========" + Console.RESET)

    val contract: ErgoContract = PlayerProxyContractBuilder(setupConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    // write to the report
    reportConfig.playerProxyBox.playerProxyContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

}