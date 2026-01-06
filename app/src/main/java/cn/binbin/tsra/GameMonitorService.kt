package cn.binbin.tsra

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import java.io.BufferedReader
import java.io.InputStreamReader

class GameMonitorService : Service() {

    private val gamePackages = setOf(
        "com.tencent.jkchess",
        "com.tencent.letsgo",
        "com.tencent.lolm",
        "com.tencent.mf.uam",
        "com.tencent.nba2kx",
        "com.tencent.nfsonline",
        "com.tencent.tmgp.WePop",
        "com.tencent.tmgp.bh3",
        "Nekootan.kfkj",
        "age.of.civilizations2.jakowski.lukasz",
        "brownmonster.app.game.rushrally3",
        "com.AlfaBravo.Combat",
        "com.BlackSaltGames.DREDGE",
        "com.CarXTech.highWay",
        "com.ChillyRoom.DungeonShooter",
        "com.EtherGaming.PocketRogues",
        "com.Flanne.MinutesTillDawn.roguelike.shooting.gp",
        "com.GameCoaster.ProtectDungeon",
        "com.GlobalSoFunny.Sausage",
        "com.HoYoverse.hkrpgoversea",
        "com.LanPiaoPiao.PlantsVsZombiesRH",
        "com.MOBGames.PoppyMobileChap1",
        "com.ProjectMoon.LimbusCompany",
        "com.RoamingStar.BlueArchive",
        "com.Roboatino.ShogunShowdown",
        "com.ShinyShoe.MonsterTrain.mtap",
        "com.Shooter.ModernWarfront",
        "com.Shooter.ModernWarships",
        "com.Sunborn.SnqxExilium",
        "com.TechTreeGames.TheTower",
        "com.YiMoXuan.XYZJF",
        "com.YoStarEN.Arknights",
        "com.YoStarEN.MahjongSoul",
        "com.YoStarJP.MajSoul",
        "com.YostarJP.BlueArchive",
        "com.ZeroCastleGameStudio.StrikeBusterPrototype",
        "com.ZeroCastleGameStudioINTL.StrikeBusterPrototype",
        "com.actgames.bbee",
        "com.activision.callofduty.shooter",
        "com.activision.callofduty.warzone",
        "com.albiononline",
        "com.aligames.kuang.kybc",
        "com.and.games505.Terraria",
        "com.axlebolt.standoff2",
        "com.bairimeng.dmmdzz",
        "com.bandainamcoent.idolmaster_gakuen",
        "com.bandainamcoent.imas_millionlive_theaterdays",
        "com.bandainamcoent.sao",
        "com.bandainamcoent.shinycolorsprism",
        "com.bandainamcogames.dbzdokkanww",
        "com.bf.sgs.hdexp.bd",
        "com.bhvr.deadbydaylight",
        "com.bilibili.azurlane",
        "com.bilibili.deadcells.mobile",
        "com.bilibili.fatego",
        "com.bilibili.heaven",
        "com.bilibili.priconne",
        "com.bilibili.star.bili",
        "com.bilibili.warmsnow",
        "com.biligamekr.aggp",
        "com.bingkolo.kleins.cn",
        "com.blade.bd",
        "com.blizzard.diablo.immortal",
        "com.blizzard.wtcg.hearthstone",
        "com.bscotch.crashlands2",
        "com.bushiroad.d4dj",
        "com.bushiroad.lovelive.schoolidolfestival2",
        "com.chucklefish.stardewvalley",
        "com.cnvcs.xiangqi",
        "com.crunchyroll.gv.shogunshowdown.game",
        "com.crunchyroll.princessconnectredive",
        "com.denachina.g13002010",
        "com.denchi.vtubestudio",
        "com.dfjz.moba",
        "com.dgames.g15002002",
        "com.dragonli.projectsnow",
        "com.dts.freefireth",
        "com.ea.game.pvz2_row",
        "com.ea.games.r3",
        "com.epicgames.fortnite",
        "com.fantablade.icey",
        "com.gaijingames.wtm",
        "com.gameloft.android.ANMP.GloftA9HM",
        "com.garena.game.codm",
        "com.garena.game.df",
        "com.garena.game.kgtw",
        "com.garena.game.nfsm",
        "com.gbits.funnyfighter.android.overseas",
        "com.gryphline.exastris.gp",
        "com.guigugame.guigubahuang",
        "com.guyou.deadstrike",
        "com.h73.jhqyna",
        "com.halo.windf.hero",
        "com.heavenburnsred",
        "com.hermes.h1game",
        "com.hermes.j1game",
        "com.hermes.mk",
        "com.herogame.gplay.punishing.grayraven.tw",
        "com.hg.lbw",
        "com.hottapkgs.hotta",
        "com.humo.yqqsqz.yw",
        "com.hypergryph.arknights",
        "com.hypergryph.exastris",
        "com.idreamsky.klbqm",
        "com.ignm.raspberrymash.jp",
        "com.ilongyuan.cytus2",
        "com.ilongyuan.implosion",
        "com.infoldgames.infinitynikkien",
        "com.jacksparrow.jpmajiang",
        "com.je.skgame",
        "com.jumpw.mobile300",
        "com.kiloo.subwaysurf.gw",
        "com.komoe.kmumamusumegp",
        "com.krafton.dndm",
        "com.kurogame.aki",
        "com.kurogame.haru",
        "com.kurogame.mingchao",
        "com.kurogame.wutheringwaves.global",
        "com.leiting.wf",
        "com.levelinfinite.sgameGlobal",
        "com.littleleogames.astreasixsidedoracles",
        "com.lilithgames.hgame.cn",
        "com.lilithgames.solarland.android.cn",
        "com.longe.allstarhmt",
        "com.madfingergames.legends",
        "com.miHoYo.GenshinImpact",
        "com.miHoYo.Nap",
        "com.miHoYo.Yuanshen",
        "com.miHoYo.bh3",
        "com.miHoYo.enterprise.NGHSoD",
        "com.miHoYo.hkrpg",
        "com.miHoYo.ys",
        "com.miHoYo.zenless",
        "com.minidragon.idlefantasy",
        "com.miniworldgame.creata.vn",
        "com.miraclegames.farlight84",
        "com.mobile.legends",
        "com.modx.daluandou",
        "com.mojang.minecraftpe",
        "com.movtery.zalithlauncher",
        "com.nanostudios.games.twenty.minutes",
        "com.neowizgames.game.browndust2",
        "com.netease.AVALON",
        "com.netease.EVE",
        "com.netease.aceracer",
        "com.netease.allstar",
        "com.netease.dfjs",
        "com.netease.dunkcd",
        "com.netease.dwrg",
        "com.netease.eve.en",
        "com.netease.g78na",
        "com.netease.g93na",
        "com.netease.h73hmt",
        "com.netease.h75na",
        "com.netease.hyxd",
        "com.netease.idv",
        "com.netease.jddsaef",
        "com.netease.ko",
        "com.netease.l22",
        "com.netease.lglr",
        "com.netease.ma84",
        "com.netease.moba",
        "com.netease.mrzh",
        "com.netease.newspike",
        "com.netease.nshm",
        "com.netease.nshmhmt",
        "com.netease.onmyoji",
        "com.netease.party",
        "com.netease.pes",
        "com.netease.qrsj",
        "com.netease.race",
        "com.netease.sky",
        "com.netease.soulofhunter",
        "com.netease.tj",
        "com.netease.tom",
        "com.netease.wotb",
        "com.netease.wyclx",
        "com.netease.x19",
        "com.netease.yhtj",
        "com.netease.yyslscn",
        "com.netease.yzs",
        "com.nexon.bluearchive",
        "com.nexon.kartdrift",
        "com.nexon.mdnf",
        "com.nexon.mod",
        "com.nianticlabs.monsterhunter",
        "com.nianticproject.ingress",
        "com.oninou.FAPI",
        "com.papegames.infinitynikki",
        "com.pi.czrxdfirst",
        "com.pinkcore.tkfm",
        "com.playdigious.deadcells.mobile",
        "com.playmini.miniworld",
        "com.proxima.dfm",
        "com.proximabeta.nikke",
        "com.prpr.musedash",
        "com.pubg",
        "com.pubg.newstate",
        "com.pwrd.hotta.laohu",
        "com.pwrd.huanta",
        "com.pwrd.p5x",
        "com.pwrd.persona5x.laohu",
        "com.r2games.myhero.bilibili",
        "com.rayark.cytus2",
        "com.rayark.implosion",
        "com.rekoo.pubgm",
        "com.riotgames.league.teamfighttactics",
        "com.riotgames.league.wildrift",
        "com.roblox.client",
        "com.savysoda.pss2.android",
        "com.seasun.jx3",
        "com.sega.ColorfulStage.en",
        "com.sega.pjsekai",
        "com.shangyoo.neon",
        "com.shatteredpixel.shatteredpixeldungeon",
        "com.shenlan.m.reverse1999",
        "com.sofunny.Sausage",
        "com.soulgamechst.majsoul",
        "com.supercell.boombeach",
        "com.supercell.clashroyale",
        "com.supercell.moco",
        "com.sy.dldlhsdj",
        "com.t2ksports.nba2k20and",
        "com.tencent.KiHan",
        "com.tencent.hhw",
        "com.tencent.ig",
        "com.tencent.tmgp.cf",
        "com.tencent.tmgp.cod",
        "com.tencent.tmgp.codev",
        "com.tencent.tmgp.dfjs",
        "com.tencent.tmgp.dfm",
        "com.tencent.tmgp.djsy",
        "com.tencent.tmgp.dnf",
        "com.tencent.tmgp.dwrg",
        "com.tencent.tmgp.ffom",
        "com.tencent.tmgp.gnyx",
        "com.tencent.tmgp.kr.codm",
        "com.tencent.tmgp.pubgmhd",
        "com.tencent.tmgp.sgame",
        "com.tencent.tmgp.speedmobile",
        "com.tencent.tmgp.sskgame",
        "com.tencent.tmgp.supercell.boombeach",
        "com.tencent.tmgp.wuxia",
        "com.tencent.tmgp.yys.zqb",
        "com.tencent.toaa",
        "com.tgc.sky.android",
        "com.the10tons.dysmantle",
        "com.tipsworks.android.pascalswager",
        "com.tipsworks.pascalswager",
        "com.trampolinetales.lbal",
        "com.tungsten.fcl",
        "com.ubisoft.rainbowsixmobile.r6.fps.pvp.shooter",
        "com.unity.mmd",
        "com.valvesoftware.cswgsm",
        "com.valvesoftware.source",
        "com.vng.pubgmobile",
        "com.xd.TLglobal",
        "com.xd.dxlzz.taptap",
        "com.xd.rotaeno.googleplay",
        "com.xd.rotaeno.tapcn",
        "com.xd.terraria",
        "com.xd.xdt",
        "com.xindong.torchlight",
        "com.xjskp.tv.hnsc",
        "com.yinhan.hunter",
        "com.yongshi.tenojo",
        "com.zlongame.mhmnz",
        "com.ztgame.bob",
        "com.ztgame.yyzy",
        "com.zy.wqmt.cn",
        "gplay.punishing.grayraven",
        "jp.co.bandainamcoent.BNEI0242",
        "jp.co.craftegg.band",
        "jp.co.cygames.princessconnectredive",
        "jp.konami.masterduel",
        "jp.konami.pesam",
        "me.mugzone.emiria",
        "me.tigerhix.cytoid",
        "minitech.miniworld",
        "moe.low.arc",
        "net.kdt.pojavlaunch",
        "net.wargaming.wot.blitz",
        "org.flos.phira",
        "org.maxbytes.lfs",
        "sh.ppy.osulazer",
        "supercell.brawlstars",
        "supercell.clashofclans",
        "supercell.squad",
        "tw.sonet.allbw",
        "tw.sonet.princessconnect",
        "xd.sce.promotion"
    )

    @Volatile
    private var running = false
    private var worker: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!running) {
            running = true
            worker = Thread { monitorLoop() }.also { it.start() }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        worker?.interrupt()
        worker = null
        super.onDestroy()
    }

    private fun monitorLoop() {
        var lastGamePackage: String? = null
        while (running) {
            try {
                val currentGamePackage = getRunningGamePackageByPs()
                val inGame = currentGamePackage != null
                if (inGame && currentGamePackage != lastGamePackage) {
                    // 检测到有指定游戏进程正在运行，自动切换到 360Hz
                    TouchRunner.run(applicationContext, 360) { success, code ->
                        if (success) {
                            showToast(getString(R.string.toast_auto_game_360))
                        } else {
                            showToast(getString(R.string.toast_cmd_fail, code))
                        }
                    }
                }
                lastGamePackage = if (inGame) currentGamePackage else null
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                break
            } catch (_: Exception) {
                // 忽略单次轮询异常，继续循环
            }
        }
    }

    private fun getRunningGamePackageByPs(): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "ps"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = reader.readLine()

            // 跳过表头
            if (line != null && line.contains("PID") && line.contains("NAME")) {
                line = reader.readLine()
            }

            while (line != null) {
                val columns = line.trim().split(Regex("\\s+"))
                if (columns.isNotEmpty()) {
                    val procName = columns.last()
                    if (gamePackages.contains(procName)) {
                        return procName
                    }
                }
                line = reader.readLine()
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}
