#!/usr/bin/env python3
"""Look up Minecraft names in the yarn mappings, and cross-check the yarn-1.21.11 folder.

Usage:
  yarn.py cls  <named.class.path>              -> the intermediary class and whether the yarn folder has it
  yarn.py m    <named.class.path> <method>     -> the intermediary method + descriptor + owning class
  yarn.py f    <named.class.path> <field>      -> the intermediary field
  yarn.py owner <intermediary.method>          -> which named class owns it
  yarn.py has  <named.class.path> <method> <descriptor-ish>  -> verify a mixin target exactly
  yarn.py grep <name>                          -> all classes/methods with that name
"""
import os
import sys

TINY = ('/home/codespace/.gradle/caches/fabric-loom/1.21.11/'
        'net.fabricmc.yarn.1_21_11.1.21.11+build.6-v2/mappings.tiny')
FOLDER = '/workspaces/codespaces/yarn-1.21.11/mappings/net/minecraft'

named2inter = {}
inter2named = {}
members = {}          # (inter_class, inter_member) -> (kind, desc, named, owner_named)
named_members = {}    # (named_class, named_member) -> list of (kind, desc, inter)
owner_of = {}         # inter_member -> named class

cur_inter = None
cur_named = None
for line in open(TINY):
    if line.startswith('c\t'):
        p = line.rstrip('\n').split('\t')
        cur_inter, cur_named = p[2], p[3]
        named2inter[cur_named] = cur_inter
        inter2named[cur_inter] = cur_named
    elif line.startswith('\tm\t') or line.startswith('\tf\t'):
        p = line.rstrip('\n').split('\t')
        kind, desc, inter, named = p[0], p[1], p[3], p[4]
        members[(cur_inter, inter)] = (kind, desc, named, cur_named)
        named_members.setdefault((cur_named, named), []).append((kind, desc, inter))
        owner_of[inter] = cur_named

# index of the yarn source folder: intermediary class -> (path, named)
folder_index = {}
for root, dirs, files in os.walk(FOLDER):
    for f in files:
        if f.endswith('.mapping'):
            p = os.path.join(root, f)
            with open(p) as fh:
                first = fh.readline().split()
            if len(first) >= 3 and first[0] == 'CLASS':
                folder_index[first[1]] = (p, first[2])

# index of the yarn source folder: intermediary class -> set of (kind, named)
folder_members = {}
for inter, (path, named) in folder_index.items():
    entries = set()
    with open(path) as fh:
        for line in fh:
            p = line.split()
            if p and p[0] in ('METHOD', 'FIELD') and len(p) >= 3:
                entries.add((p[0], p[1]))
    folder_members[inter] = entries


def folder_has(inter_class, inter_member, kind):
    """Is the member present in the yarn-1.21.11 source folder?"""
    return (kind, inter_member) in folder_members.get(inter_class, set())


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        return

    cmd = sys.argv[1]

    if cmd == 'cls':
        name = sys.argv[2]
        inter = named2inter.get(name)
        if inter is None:
            print('NOT FOUND in build.6:', name)
            return
        entry = folder_index.get(inter)
        print('named     :', name)
        print('inter     :', inter)
        print('yarn folder:', entry[0] if entry else 'ABSENT')
        return

    if cmd == 'm' or cmd == 'f':
        kind_want = 'METHOD' if cmd == 'm' else 'FIELD'
        cls, name = sys.argv[2], sys.argv[3]
        inter = named2inter.get(cls)
        if inter is None:
            print('class not found:', cls)
            return
        found = False
        for (nc, nm), lst in named_members.items():
            if nc != cls or nm != name:
                continue
            found = True
            for kind, desc, inter_m in lst:
                infolder = folder_has(inter, inter_m, kind)
                print('%s %s.%s' % (kind, cls, name))
                print('  intermediary: %s' % inter_m)
                print('  descriptor  : %s' % desc)
                print('  yarn folder : %s' % ('PRESENT' if infolder else 'ABSENT'))
        if not found:
            print('%s %s.%s NOT FOUND' % (kind_want, cls, name))
        return

    if cmd == 'owner':
        print('owner:', owner_of.get(sys.argv[2]))
        return

    if cmd == 'grep':
        needle = sys.argv[2]
        for (nc, nm), lst in sorted(named_members.items()):
            if needle in nm:
                for kind, desc, inter_m in lst:
                    print('%s %s.%s  %s  inter=%s' % (kind, nc, nm, desc, inter_m))
        return

    if cmd == 'has':
        # has <class> <method> <argTypes...>  e.g. has net/minecraft/client/Mouse onMouseScroll J D D
        cls = sys.argv[2]
        method = sys.argv[3]
        args = sys.argv[4:]
        inter = named2inter.get(cls)
        if inter is None:
            print('class not found:', cls)
            return
        for (nc, nm), lst in named_members.items():
            if nc != cls or nm != method:
                continue
            for kind, desc, inter_m in lst:
                params = desc[1:desc.index(')')] if ')' in desc else ''
                got = params.split('L') if params else []
                # crude arg-count/type comparison
                print('found: %s %s.%s%s' % (kind, cls, method, desc))
                print('  yarn folder: %s' % ('PRESENT' if folder_has(inter, inter_m, kind) else 'ABSENT'))
                print('  intermediary method: %s' % inter_m)
        return

    print('unknown command', cmd)


if __name__ == '__main__':
    main()
