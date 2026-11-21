const PROTECTED = [".env", "/.git/", "application-local.yml", "/target/"]

const isProtected = (patterns: string[]) =>
  patterns.some((p) => PROTECTED.some((prot) => String(p).includes(prot)))

export const ProjectGuard = async ({ $, worktree }: any) => {
  return {
    // Hook 2 — блокировка защищённых файлов (перед действием)
    "permission.ask": (input: any, output: any) => {
      if (isProtected(input?.patterns ?? [])) {
        output.status = "deny"
      }
    },

    // Hook 1 — форматтер после правки .java
    "tool.execute.after": async (input: any) => {
      const filePath = String(input?.args?.filePath ?? "")
      const t = input?.tool
      if ((t === "write" || t === "edit" || t === "patch") && filePath.endsWith(".java")) {
        try {
          await $`mvn spotless:apply -q`.cwd(worktree).nothrow()
        } catch (e) {
          console.error("spotless:", e)
        }
      }
    },
  }
}